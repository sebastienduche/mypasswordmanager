package com.passwordmanager.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "credential")
public class PasswordData {

  private String name;
  private String user;
  private String password;
  private String url;
  private String hint;
  private String comment;
  private boolean deprecated;

  public PasswordData() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isDeprecated() {
    return deprecated;
  }

  public void setDeprecated(boolean deprecated) {
    this.deprecated = deprecated;
  }

  public String getHint() {
    return hint;
  }

  public void setHint(String hint) {
    this.hint = hint;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }
}
