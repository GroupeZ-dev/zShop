package fr.maxlego08.shop.limit;

import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.api.limit.Limit;
import fr.maxlego08.shop.api.limit.LimitType;
import fr.maxlego08.shop.api.limit.SchedulerType;
import fr.maxlego08.shop.zcore.utils.builder.TimerBuilder;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class ZLimit implements Limit {

    private transient final ShopPlugin plugin;
    private transient final LimitType limitType;
    private transient final int limit;

    private transient final SchedulerType schedulerType;
    private transient final int dayOfMonth;
    private transient final int dayOfWeek;
    private transient final int month;
    private transient final int hour;
    private transient final int minute;
    private final String material;
    private int id;
    private int amount;
    private transient boolean isValid;
    private transient Calendar calendar;

    // Ajouter un moyen de supprimer la limite si elle n'existe plus

    public ZLimit(ShopPlugin plugin, LimitType limitType, String material, int limit, SchedulerType schedulerType, int dayOfMonth, int dayOfWeek, int month, int hour, int minute) {
        this.plugin = plugin;
        this.limitType = limitType;
        this.material = material;
        this.limit = limit;
        this.schedulerType = schedulerType;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.month = month;
        this.hour = hour;
        this.minute = minute;

        this.generateCalendar();
    }

    private void generateCalendar() {
        Calendar now = new GregorianCalendar();
        switch (schedulerType) {
            case HOURLY:
                calendar = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY), minute);
                break;
            case DAILY:
                calendar = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), hour, minute);
                break;
            case WEEKLY:
                calendar = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), hour, minute);
                int currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK);
                int daysUntilNextTargetDay = (dayOfWeek - currentDayOfWeek + 7) % 7;

                calendar.add(Calendar.DAY_OF_MONTH, daysUntilNextTargetDay);
                break;
            case MONTHLY:
                calendar = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), dayOfMonth, hour, minute);
                break;
            case YEARLY:
                calendar = new GregorianCalendar(now.get(Calendar.YEAR), month, dayOfMonth, hour, minute);
                break;
        }

        if (calendar.before(now)) {
            nextScheduler();
        }
    }

    private void nextScheduler() {
        switch (schedulerType) {
            case HOURLY:
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                break;
            case DAILY:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case WEEKLY:
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                break;
            case MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                break;
            case YEARLY:
                calendar.add(Calendar.YEAR, 1);
                break;
        }
    }

    @Override
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public LimitType getType() {
        return this.limitType;
    }

    @Override
    public String getMaterial() {
        return this.material;
    }

    @Override
    public int getLimit() {
        return this.limit;
    }

    @Override
    public int getAmount() {

        // Check if reset is needed
        this.update();

        return this.amount;
    }

    @Override
    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    @Override
    public SchedulerType getSchedulerType() {
        return schedulerType;
    }

    @Override
    public int getDayOfMonth() {
        return dayOfMonth;
    }

    @Override
    public int getDayOfWeek() {
        return dayOfWeek;
    }

    @Override
    public int getMonth() {
        return month;
    }

    @Override
    public int getHour() {
        return hour;
    }

    @Override
    public int getMinute() {
        return minute;
    }

    @Override
    public Calendar getCalendar() {
        return this.calendar;
    }

    @Override
    public String getFormattedTimeUntilNextTask() {
        return TimerBuilder.getStringTime((calendar.getTimeInMillis() - new GregorianCalendar().getTimeInMillis()) / 1000);
    }

    @Override
    public void update() {
        if (calendar != null && schedulerType != SchedulerType.NEVER && System.currentTimeMillis() >= calendar.getTimeInMillis()) {

            if (this.limitType == LimitType.SERVER_BUY || this.limitType == LimitType.SERVER_SELL) amount = 0;
            else {
                plugin.getLimiterManager().reset(this);
            }
            nextScheduler();
        }
    }

    @Override
    public String toString() {
        return "ZLimit{" +
                "plugin=" + plugin +
                ", limitType=" + limitType +
                ", limit=" + limit +
                ", schedulerType=" + schedulerType +
                ", dayOfMonth=" + dayOfMonth +
                ", dayOfWeek=" + dayOfWeek +
                ", month=" + month +
                ", hour=" + hour +
                ", minute=" + minute +
                ", material='" + material + '\'' +
                ", id=" + id +
                ", amount=" + amount +
                ", isValid=" + isValid + '}';
    }
}
