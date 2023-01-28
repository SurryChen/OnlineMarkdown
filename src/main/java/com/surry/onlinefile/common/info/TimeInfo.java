package com.surry.onlinefile.common.info;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Time;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
@Getter
@AllArgsConstructor
public enum TimeInfo {

    LOGIN_TIME_TTL(30, TimeUnit.MINUTES);

    int length;
    TimeUnit timeUnit;

}
