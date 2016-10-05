package com.example.caleb.myjourney;

import org.json.JSONException;
import org.json.JSONObject;


public class Flight {
    private String statusText, scheduled, terminal, city, gate;

    // not sure if these information are needed:
    // private String status, estimated, cityCode;


    public Flight(JSONObject flightInfo) {
        try {
            // status = flightInfo.getString("status");
            statusText = flightInfo.getString("statusText");
            scheduled = flightInfo.getString("scheduled");
            //gate = flightInfo.getString("gate");
            terminal = flightInfo.getString("terminal");
            city = flightInfo.getString("city");
            // cityCode = flightInfo.getString("cityCode");
            // estimated = flightInfo.getString("estimated");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getStatusText() {
        return statusText;
    }

    public String getScheduled() {
        return scheduled;
    }

    public String getGate() {
        return gate;
    }

    public String getTerminal() {
        return terminal;
    }

    public String getCity() {
        return city;
    }

    /* public String getCityCode() {
        return cityCode;
    }

    public String getStatus() {
            return status;
        }
    public String getEstimated() {
        return estimated;
    } */
}
