package io.awesome.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class PropertyHelper {
  @Value("${meta.loginTitle}")
  public String loginTitle;

  @Value("${meta.loginDescription}")
  public String loginDescription;

  @Value("${app.finance.email}")
  public String financeEmail;

  @Value("${app.upload.path}")
  public String uploadPath;
  @Value("${user.default.password}")
  public String userDefaultPassword;
  public boolean appIsSendEmail;

  public String mailHost;
  public String mailPort;
  public String mailBounce;
  public String mailFrom;
  public String mailFromName;
  public String mailUsername;
  public String mailPassword;
}
