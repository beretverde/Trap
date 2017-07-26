package com.renegade.trap;

import android.content.Intent;
import android.view.View;

/**
 * Created by brent on 7/10/17.
 */

public class PdfInfoDTO {
    View view;
    String workId;
    String cityName;
    String locName;
    Intent email;

    public PdfInfoDTO(View view, String workId, String cityName, String locName, Intent email) {
        this.view = view;
        this.workId = workId;
        this.cityName = cityName;
        this.locName = locName;
        this.email = email;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public String getWorkId() {
        return workId;
    }

    public void setWorkId(String workId) {
        this.workId = workId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public Intent getEmail() {
        return email;
    }

    public void setEmail(Intent email) {
        this.email = email;
    }
}
