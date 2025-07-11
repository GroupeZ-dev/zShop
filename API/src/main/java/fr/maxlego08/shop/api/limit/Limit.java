package fr.maxlego08.shop.api.limit;

import java.util.Calendar;

public interface Limit {

    int getId();

    LimitType getType();

    String getMaterial();

    int getLimit();

    int getAmount();

    void setAmount(int amount);

    SchedulerType getSchedulerType();

    int getDayOfMonth();

    int getDayOfWeek();

    int getMonth();

    int getHour();

    int getMinute();

    Calendar getCalendar();

    String getFormattedTimeUntilNextTask();

    void update();

}
