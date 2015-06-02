package test;

import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;
import java.util.Map;

@ContentType("ctid")
public class BaseFields extends Resource {
  @Field
  String text;

  @Field
  String symbol;

  @Field
  Integer number;

  @Field
  Double decimal;

  @Field
  Boolean yesNo;

  @Field
  String dateTime;

  @Field
  Map location;

  @Field
  Map obj;

  public String text() {
    return text;
  }

  public String symbol() {
    return symbol;
  }

  public Integer number() {
    return number;
  }

  public Double decimal() {
    return decimal;
  }

  public Boolean yesNo() {
    return yesNo;
  }

  public String dateTime() {
    return dateTime;
  }

  public Map location() {
    return location;
  }

  public Map obj() {
    return obj;
  }
}
