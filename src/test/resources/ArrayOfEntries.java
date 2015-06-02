package test;

import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;
import java.util.List;

@ContentType("ctid")
public class ArrayOfEntries extends Resource {
  @Field
  List<LinkedResource> entries;

  public List<LinkedResource> entries() {
    return entries;
  }
}
