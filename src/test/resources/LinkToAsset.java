package test;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;

@ContentType("ctid")
public class LinkToAsset {
  @Field
  Asset asset;

  public Asset asset() {
    return asset;
  }
}
