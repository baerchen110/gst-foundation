/*
 * Copyright 2010 FatWire Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fatwire.gst.foundation.tagging;

import COM.FutureTense.CS.Factory;

import com.fatwire.assetapi.data.AssetId;
import com.openmarket.basic.event.AbstractAssetEventListener;

/**
 * Sends requests to the tagging service.
 *
 * @author Tony Field
 * @since Jul 28, 2010
 */
public final class TaggedAssetEventListener extends AbstractAssetEventListener {

    private final AssetTaggingService svc;

    public TaggedAssetEventListener() {
        try {
            svc = AssetTaggingServiceFactory.getService(Factory.newCS());
        } catch (Exception e) {
            throw new IllegalStateException("Could not create ICS", e);
        }
    }


    @Override
    public void assetAdded(AssetId assetId) {
        svc.addAsset(assetId);
    }

    @Override
    public void assetUpdated(AssetId assetId) {
        svc.updateAsset(assetId);
    }

    @Override
    public void assetDeleted(AssetId assetId) {
        svc.deleteAsset(assetId);
    }
}
