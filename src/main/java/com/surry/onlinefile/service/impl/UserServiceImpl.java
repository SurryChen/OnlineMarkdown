package com.surry.onlinefile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.surry.onlinefile.common.UserHolder;
import com.surry.onlinefile.common.info.BaseInfo;
import com.surry.onlinefile.common.info.UserInfo;
import com.surry.onlinefile.dao.UserDao;
import com.surry.onlinefile.entity.po.User;
import com.surry.onlinefile.service.UserService;
import com.surry.onlinefile.utils.RedisUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 这里也继承了IService，所以批处理也是可以做的
 * 如果不想用UserDao，直接用IService里面的方法也是可以的
 * 两者没差多少，只是IService多了一些批处理的操作
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserDao userDao;

    @SneakyThrows
    @Override
    public BaseInfo register(User user) {
        // 一个邮箱只能注册一个账号
        // 先去Redis中寻找是否有该账号
        User account = (User) RedisUtil.findBean(user.getUserEmail(), new User());
        // 如果有账号，直接返回，该账号已经被注册
        // 就算redis中没有，经过User类型的强转后，account都不会为空，只是里面内容为空
        if (account.getUserId() != null) {
            return UserInfo.MAILBOX_FOUND;
        } else {
            // 如果没有账号，再去数据库中寻找是否有这个账号
            // 如果数据库中有这个账号，直接返回，该账号已经被注册
            // 但是也是需要同步的
            LambdaQueryWrapper<User> lam = new LambdaQueryWrapper();
            lam.eq(User::getUserEmail, user.getUserEmail());
            User userSelect = userDao.selectOne(lam);
            if (userSelect != null) {
                // 只要是查数据库都是要同步的
                RedisUtil.saveBean(userSelect.getUserId(), userSelect);
                RedisUtil.saveBean(userSelect.getUserEmail(), userSelect);
                return UserInfo.MAILBOX_FOUND;
            } else {
                // 生成一个id
//                Long id = IdUtil.getId();
//                user.setUserId(id);
                userDao.insert(user);
                RedisUtil.saveBean(user.getUserId(), user);
                RedisUtil.saveBean(user.getUserEmail(), user);
                return UserInfo.REGISTER_SUCCESS;
            }
        }
    }

    /**
     * 把设置token放置在了controller，service提供数据查找、数据同步，controller可以针对结果做一些处理
     * 如果service本身业务不需要返回数据的，但是又需要处理的一些数据，交由controller处理
     * 比如下面的登录就是，能不能登录不用返回什么数据，但是又需要处理token的问题，像这种就交给controller处理
     */
    @SneakyThrows
    @Override
    public BaseInfo login(User user) {

        // 先找到对应的用户
        User userByEmail = findUserByEmail(user.getUserEmail());
        // 看密码对不对
        if(userByEmail == null) {
            // 说明没有这个用户
            return UserInfo.MAILBOX_NOTFOUND;
        } else {
            // 说明有这个用户
            // 查找密码对不对
            if(userByEmail.getUserPassword().equals(user.getUserPassword())) {
                // 密码正确
                // 在看看是否已经登录过
                String token = (String) redisTemplate.opsForValue().get(user.getUserEmail() + "_token");
                if(!(token == null || "".equals(token))) {
                    // 说明已经登录过
                    UserHolder.setCurrentUserId(userByEmail.getUserId());
                    return UserInfo.SECOND_LOGIN_SUCCESS;
                }
                // 什么都没有，正常返回
                // 标志已经登录
                redisTemplate.opsForValue().set(userByEmail.getUserEmail() + "_token", "111");
                UserHolder.setCurrentUserId(userByEmail.getUserId());
                return UserInfo.LOGIN_SUCCESS;
            } else {
                // 密码错误
                return UserInfo.PASSWORD_WRONG;
            }
        }

    }

    /**
     * 修改信息都要先修改数据库，因为如果不是先修改数据库，一旦修改redis后立刻有人查询
     * 此时数据库没有修改成功，那么redis中的数据依旧是旧数据
     */
    @SneakyThrows
    @Override
    public BaseInfo modifyUsername(String username) {
        // 先修改数据库
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("user_id", UserHolder.getCurrentUserId());
        userUpdateWrapper.set("user_name", username);
        update(userUpdateWrapper);
        // 修改完数据库，就删除redis中的键，等到查询的时候再更新
        // 需要找出user对象
        User user = findUserById(UserHolder.getCurrentUserId());
        redisTemplate.delete(user.getUserId());
        redisTemplate.delete(user.getUserEmail());
        // 因为有修改数据就需要把之前的缓存删掉，所以要注意好，在哪些地方将需要修改的信息放入了缓存
        return UserInfo.USERNAME_MODIFY_SUCCESS;
    }

    @Override
    public BaseInfo modifyPassword(String password) {
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("user_id", UserHolder.getCurrentUserId());
        userUpdateWrapper.set("user_password", password);
        update(userUpdateWrapper);
        // 需要找出user对象
        User user = findUserById(UserHolder.getCurrentUserId());
        redisTemplate.delete(user.getUserId());
        redisTemplate.delete(user.getUserEmail());
        // 返回成功
        return UserInfo.PASSWORD_MODIFY_SUCCESS;
    }

    @Override
    public BaseInfo modifyPortraitPath(String portraitPath) {
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("user_id", UserHolder.getCurrentUserId());
        userUpdateWrapper.set("portrait_path", portraitPath);
        update(userUpdateWrapper);
        // 需要找出user对象
        User user = findUserById(UserHolder.getCurrentUserId());
        redisTemplate.delete(user.getUserId());
        redisTemplate.delete(user.getUserEmail());
        // 返回成功
        return UserInfo.PASSWORD_MODIFY_SUCCESS;
    }

    @SneakyThrows
    @Override
    public User findUserById(Long userId) {
        // 先查询redis，如果redis中存在就直接返回
        User findUser = (User) RedisUtil.findBean(userId, new User());
        if (findUser.getUserId() != null) {
            return findUser;
        }
        // 如果redis中不存在，则查询数据库
        // 这个不一定可以，因为我的实体类中没有标注哪一个是id
        findUser = userDao.selectById(userId);
        if (findUser != null) {
            // 如果数据库中存在，则写入到redis中，然后返回
            RedisUtil.saveBean(findUser.getUserId(), findUser);
            RedisUtil.saveBean(findUser.getUserEmail(), findUser);
            return findUser;
        }
        // 说明不存在该用户
        return null;
    }

    @SneakyThrows
    @Override
    public User findUserByEmail(String email) {
        // 先查询redis，如果redis中存在就直接返回
        User findUser = (User) RedisUtil.findBean(email, new User());
        if (findUser.getUserId() != null) {
            return findUser;
        }
        // 如果redis中不存在，则查询数据库
        LambdaQueryWrapper<User> lam = new LambdaQueryWrapper();
        lam.eq(User::getUserEmail, email);
        findUser = userDao.selectOne(lam);
        if (findUser != null) {
            // 如果数据库中存在，则写入到redis中，然后返回
            RedisUtil.saveBean(findUser.getUserId(), findUser);
            RedisUtil.saveBean(findUser.getUserEmail(), findUser);
            return findUser;
        }
        // 说明不存在该用户
        return null;
    }


}
