package org.fao.fenix.d3s.cache.tools;


public interface Server {

    public static final String CONFIG_FOLDER_PATH = "cache/config/";


    public void start() throws Exception;
    public void stop();
}
