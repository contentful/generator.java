package test;

import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import java.util.List;

@ContentType("ctid")
public class ArrayOfEntries {
  @Field
  List<LinkedResource> entries;

  public List<LinkedResource> entries() {
    return entries;
  }
}
