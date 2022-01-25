package com.sobey.jcg.support.nordb;

import com.sobey.jcg.support.sys.podo.DataSrcPO;


public abstract class NoRDBDataSource {

    protected DataSrcPO srcPO;

    protected NoRDBDataSource(DataSrcPO srcPO) {
        this.srcPO = srcPO;
    }

}
