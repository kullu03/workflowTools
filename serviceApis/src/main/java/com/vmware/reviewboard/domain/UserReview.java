package com.vmware.reviewboard.domain;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class UserReview extends BaseEntity {

    @SerializedName(value = "public")
    public boolean isPublic;

    public boolean ship_it;

    public String body_top;

    public Date timestamp;

    public String getReviewUsername() {
        return getLink("user").getTitle();
    }
}
