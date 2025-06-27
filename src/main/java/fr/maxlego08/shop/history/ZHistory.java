package fr.maxlego08.shop.history;

import fr.maxlego08.shop.api.history.History;
import fr.maxlego08.shop.api.history.HistoryType;
import fr.maxlego08.shop.save.LogConfig;
import fr.maxlego08.shop.zcore.utils.MessageUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ZHistory implements History {

    private final HistoryType type;
    private final String message;
    private final long date;

    public ZHistory(HistoryType type, String message) {
        super();
        this.type = type;
        this.message = MessageUtils.removeColorCodes(message);
        this.date = System.currentTimeMillis();
    }

    @Override
    public long getDate() {
        return date;
    }

    @Override
    public HistoryType getType() {
        return type;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDateAsString() {
        return new SimpleDateFormat(LogConfig.dateFormatLog).format(new Date(this.date));
    }

}
