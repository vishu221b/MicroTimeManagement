package com.microtimemanagement.apiservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Calendar;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum TimeMeridian {
    AM(Calendar.AM),
    PM(Calendar.PM);

    private Integer value;

    public static TimeMeridian valueOf(Integer input){
        for(var tm: TimeMeridian.values()){
            if(input.equals(tm.getValue())){
                return tm;
            }
        }return null;
    }

    public static TimeMeridian getOppositeOf(Integer input){
        if(input.equals(TimeMeridian.AM.getValue())){
            return TimeMeridian.PM;
        }
        return TimeMeridian.AM;

    }
}
