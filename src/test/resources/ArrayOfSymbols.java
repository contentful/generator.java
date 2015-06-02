package test;

import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import java.util.List;

@ContentType("ctid")
public class ArrayOfSymbols {
  @Field
  List symbols;

  public List symbols() {
    return symbols;
  }
}
