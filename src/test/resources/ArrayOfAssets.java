package test;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;
import java.util.List;

@ContentType("ctid")
public class ArrayOfAssets extends Resource {
  @Field
  List<Asset> arrayOfAssets;

  public List<Asset> arrayOfAssets() {
    return arrayOfAssets;
  }
}
