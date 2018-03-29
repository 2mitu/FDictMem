package com.ffl.felix.string;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by PengfeiLin on 2018/1/18.
 */

public class SuffixFilenameFilter implements FilenameFilter {
    private String fileSuffix;
    public SuffixFilenameFilter(String suffix) {
        fileSuffix = suffix;
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(fileSuffix);
    }
}
