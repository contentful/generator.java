package test;

import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;
import java.util.List;

@ContentType("ctid")
public class ArrayOfSymbols extends Resource {
  @Field
  List symbols;

  public List symbols() {
    return symbols;
  }
}
