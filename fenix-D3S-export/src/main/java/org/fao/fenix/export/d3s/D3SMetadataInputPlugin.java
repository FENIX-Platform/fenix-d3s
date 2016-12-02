package org.fao.fenix.export.d3s;

import com.fasterxml.jackson.databind.JsonNode;
import org.fao.fenix.commons.msd.dto.data.Resource;
import org.fao.fenix.commons.msd.dto.full.DSDDataset;
import org.fao.fenix.commons.msd.dto.full.MeIdentification;
import org.fao.fenix.commons.utils.FileUtils;
import org.fao.fenix.commons.utils.JSONUtils;
import org.fao.fenix.d3s.mdsd.MDSDGenerator;
import org.fao.fenix.d3s.msd.services.rest.ResourcesService;
import org.fao.fenix.export.core.dto.data.CoreData;
import org.fao.fenix.export.core.dto.data.CoreMetaData;
import org.fao.fenix.export.core.input.plugin.Input;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class D3SMetadataInputPlugin extends Input {
    String uid;
    String version;
    ResourcesService service;
    private Map<String, Object> config;
    private final String PROPERTIES_FILE = "templates/templates.properties";

    private final String TEMPLATE = "template";

    @Override
    public void init(Map<String, Object> config, Resource resource) {
        this.config = config;
        MeIdentification metadata = resource != null ? resource.getMetadata() : null;
        uid = metadata != null ? metadata.getUid() : null;
        version = metadata != null ? metadata.getVersion() : null;
        service = ExportManager.getResourcesService(config.containsKey("lang") ? (String) config.get("lang") : "EN");
    }

    @Override
    public CoreData getResource() {
        try {
            final MeIdentification<DSDDataset> metadata = service.loadMetadata(uid, version);
            if (metadata != null) {
                Properties properties = getProperties();
                JsonNode mdsd = existsTemplate(properties) ? JSONUtils.decode(new FileUtils().readTextFile(properties.get(this.config.get(TEMPLATE)).toString()), JsonNode.class) : JSONUtils.decode(new MDSDGenerator().generate(), JsonNode.class);
                return new CoreMetaData(metadata, null, mdsd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    //UTILS
    private boolean existsTemplate(Properties properties) throws IOException {
        return this.config != null && this.config.containsKey(TEMPLATE) && this.config.get(TEMPLATE) != null && this.config.get(TEMPLATE) != ""
                && properties.containsKey(this.config.get(TEMPLATE));
    }


    private java.util.Properties getProperties() {
        java.util.Properties properties = new java.util.Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            InputStream input = classLoader.getResourceAsStream(PROPERTIES_FILE);
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }
}
