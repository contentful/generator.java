package test;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import java.util.List;

@ContentType("ctid")
public class ArrayOfAssets {
  @Field
  List<Asset> arrayOfAssets;

  public List<Asset> arrayOfAssets() {
    return arrayOfAssets;
  }
}
