package com.surry.onlinefile.common;

import com.surry.onlinefile.common.info.ControllerInfo;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

// 控制器的异常处理器
@RestControllerAdvice
public class ProjectExceptionAdvice {

    //拦截所有的异常信息
    @ExceptionHandler(Exception.class)
    public ApiMsg doException(Exception ex){
        //记录日志
        //通知运维
        //通知开发
        ex.printStackTrace();
        /*StringWriter stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        stringWriter.toString()
        */
        return new ApiMsg(ControllerInfo.SERVER_WRONG);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiMsg doHttp(Exception ex){
        //记录日志
        //通知运维
        //通知开发
        ex.printStackTrace();
        /*StringWriter stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        stringWriter.toString()
        */
        return new ApiMsg(ControllerInfo.HTTP_WRONG);
    }


    @ExceptionHandler(MissingServletRequestParameterException .class)
    public ApiMsg doHttpPara(Exception ex){
        //记录日志
        //通知运维
        //通知开发
        ex.printStackTrace();
        /*StringWriter stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        stringWriter.toString()
        */
        return new ApiMsg(ControllerInfo.NO_PARA);
    }


    @ExceptionHandler(MaxUploadSizeExceededException .class)
    public ApiMsg doFileTooBig(Exception ex){
        //记录日志
        //通知运维
        //通知开发
        ex.printStackTrace();
        /*StringWriter stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        stringWriter.toString()
        */
        return new ApiMsg(ControllerInfo.File_OVERLOAD);
    }
}
