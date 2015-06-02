package test;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

@ContentType("ctid")
public class LinkToAsset extends Resource {
  @Field
  Asset asset;

  public Asset asset() {
    return asset;
  }
}
