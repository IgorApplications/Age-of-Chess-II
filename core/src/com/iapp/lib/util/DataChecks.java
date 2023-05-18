package com.iapp.lib.util;

import com.iapp.lib.web.RequestStatus;

import java.util.List;

public class DataChecks {
    public static boolean isBadList(List<?> data) {
        return data.size() != 1;
    }

    public static RequestStatus getBadStatus(List<?> data) {
        if (data.isEmpty()) return RequestStatus.NOT_FOUND;
        if (data.size() > 1) {
            return RequestStatus.SECURITY_BREACH;
        }
        return RequestStatus.DENIED;
    }
}
