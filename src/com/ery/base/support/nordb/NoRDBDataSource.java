package com.ery.base.support.nordb;

import com.ery.base.support.sys.podo.DataSrcPO;


public abstract class NoRDBDataSource {

    protected DataSrcPO srcPO;

    protected NoRDBDataSource(DataSrcPO srcPO) {
        this.srcPO = srcPO;
    }

}
