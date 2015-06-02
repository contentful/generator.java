package test;

import com.contentful.vault.ContentType;
import com.contentful.vault.Field;

@ContentType("ctid")
public class LinkToEntry {
  @Field
  LinkedResource entry;

  public LinkedResource entry() {
    return entry;
  }
}
