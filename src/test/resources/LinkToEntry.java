package test;

import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

@ContentType("ctid")
public class LinkToEntry extends Resource {
  @Field
  LinkedResource entry;

  public LinkedResource entry() {
    return entry;
  }
}
