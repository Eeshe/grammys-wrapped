package me.eeshe.grammyswrapped.model;

import java.util.Date;

public record LoggableElectricityStatusChange(
        Date date,
        String userId,
        boolean electricityIn
        ) {

  public LoggableElectricityStatusChange(String userId, boolean electricityIn) {
    this(new Date(), userId, electricityIn);
  }
}
