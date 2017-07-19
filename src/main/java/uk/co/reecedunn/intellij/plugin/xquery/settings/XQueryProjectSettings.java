/*
 * Copyright (C) 2016 Reece H. Dunn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.reecedunn.intellij.plugin.xquery.settings;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import uk.co.reecedunn.intellij.plugin.xquery.lang.ImplementationItem;
import uk.co.reecedunn.intellij.plugin.xquery.lang.Implementations;
import uk.co.reecedunn.intellij.plugin.xquery.lang.XQueryConformance;
import uk.co.reecedunn.intellij.plugin.xquery.lang.XQueryVersion;
import uk.co.reecedunn.intellij.plugin.xquery.resources.XQueryBundle;

import java.io.File;

@State(
    name = "XQueryProjectSettings",
    storages = { @Storage(StoragePathMacros.WORKSPACE_FILE), @Storage("xquery_config.xml") }
)
public class XQueryProjectSettings implements PersistentStateComponent<XQueryProjectSettings>, ExportableComponent {
    private ImplementationItem IMPLEMENTATION = Implementations.getDefaultImplementation();
    private ImplementationItem IMPLEMENTATION_VERSION = IMPLEMENTATION.getDefaultItem(ImplementationItem.IMPLEMENTATION_VERSION);
    private XQueryVersion XQUERY_VERSION = IMPLEMENTATION_VERSION.getDefaultVersion(ImplementationItem.IMPLEMENTATION_DIALECT, XQueryConformance.MINIMAL_CONFORMANCE);
    private ImplementationItem XQUERY_1_0_DIALECT = IMPLEMENTATION_VERSION.getDefaultItemByVersion(ImplementationItem.IMPLEMENTATION_DIALECT, XQueryConformance.MINIMAL_CONFORMANCE, XQueryVersion.VERSION_1_0);
    private ImplementationItem XQUERY_3_0_DIALECT = IMPLEMENTATION_VERSION.getDefaultItemByVersion(ImplementationItem.IMPLEMENTATION_DIALECT, XQueryConformance.MINIMAL_CONFORMANCE, XQueryVersion.VERSION_3_0);
    private ImplementationItem XQUERY_3_1_DIALECT = IMPLEMENTATION_VERSION.getDefaultItemByVersion(ImplementationItem.IMPLEMENTATION_DIALECT, XQueryConformance.MINIMAL_CONFORMANCE, XQueryVersion.VERSION_3_1);

    public static XQueryProjectSettings getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, XQueryProjectSettings.class);
    }

    @Override
    public XQueryProjectSettings getState() {
        return this;
    }

    @Override
    public void loadState(XQueryProjectSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    @SuppressWarnings("NullableProblems") // jacoco Code Coverage reports an unchecked branch when @NotNull is used.
    public File[] getExportFiles() {
        return new File[]{ PathManager.getOptionsFile("xquery_project_settings") };
    }

    @Override
    @SuppressWarnings("NullableProblems") // jacoco Code Coverage reports an unchecked branch when @NotNull is used.
    public String getPresentableName() {
        return XQueryBundle.message("xquery.settings.project.title");
    }

    @Transient
    public ImplementationItem getImplementationItem() {
        return IMPLEMENTATION;
    }

    @Transient
    public void setImplementationItem(ImplementationItem implementation) {
        IMPLEMENTATION = implementation;
    }

    public String getImplementation() {
        return IMPLEMENTATION.getID();
    }

    public void setImplementation(String implementation) {
        IMPLEMENTATION = Implementations.getItemById(implementation);
    }

    @Transient
    public ImplementationItem getImplementationVersionItem() {
        return IMPLEMENTATION_VERSION;
    }

    @Transient
    public void setImplementationVersionItem(ImplementationItem version) {
        IMPLEMENTATION_VERSION = version;
    }

    public String getImplementationVersion() {
        return IMPLEMENTATION_VERSION.getID();
    }

    public void setImplementationVersion(String version) {
        IMPLEMENTATION_VERSION = Implementations.getItemById(version);
    }

    @NotNull
    public XQueryVersion getXQueryVersion() {
        return XQUERY_VERSION;
    }

    public void setXQueryVersion(@NotNull XQueryVersion version) {
        XQUERY_VERSION = version;
    }

    public String getXQuery10Dialect() {
        return XQUERY_1_0_DIALECT.getID();
    }

    public void setXQuery10Dialect(String dialect) {
        XQUERY_1_0_DIALECT = Implementations.getItemById(dialect);
    }

    public String getXQuery30Dialect() {
        return XQUERY_3_0_DIALECT.getID();
    }

    public void setXQuery30Dialect(String dialect) {
        XQUERY_3_0_DIALECT = Implementations.getItemById(dialect);
    }

    public String getXQuery31Dialect() {
        return XQUERY_3_1_DIALECT.getID();
    }

    public void setXQuery31Dialect(String dialect) {
        XQUERY_3_1_DIALECT = Implementations.getItemById(dialect);
    }

    @Transient
    public ImplementationItem getDialectForXQueryVersion(XQueryVersion version) {
        switch (version) {
            case VERSION_1_0:
                if (XQUERY_1_0_DIALECT == ImplementationItem.NULL_ITEM) {
                    return Implementations.getItemById("w3c/1.0");
                }
                return XQUERY_1_0_DIALECT;
            default:
            case VERSION_3_0:
                if (XQUERY_3_0_DIALECT == ImplementationItem.NULL_ITEM) {
                    return Implementations.getItemById("w3c/3.0");
                }
                return XQUERY_3_0_DIALECT;
            case VERSION_3_1:
                if (XQUERY_3_1_DIALECT == ImplementationItem.NULL_ITEM) {
                    return Implementations.getItemById("w3c/3.1");
                }
                return XQUERY_3_1_DIALECT;
            case VERSION_0_9_MARKLOGIC:
                final ImplementationItem default09ml = IMPLEMENTATION_VERSION.getDefaultItemByVersion(ImplementationItem.IMPLEMENTATION_DIALECT, XQueryConformance.MINIMAL_CONFORMANCE, version);
                if (default09ml == ImplementationItem.NULL_ITEM) {
                    return Implementations.getItemById("marklogic/v8/1.0-ml");
                }
                return default09ml;
            case VERSION_1_0_MARKLOGIC:
                final ImplementationItem default10ml = IMPLEMENTATION_VERSION.getDefaultItemByVersion(ImplementationItem.IMPLEMENTATION_DIALECT, XQueryConformance.MINIMAL_CONFORMANCE, version);
                if (default10ml == ImplementationItem.NULL_ITEM) {
                    return Implementations.getItemById("marklogic/v8/1.0-ml");
                }
                return default10ml;
        }
    }

    @Transient
    public void setDialectForXQueryVersion(XQueryVersion version, ImplementationItem dialect) {
        if (version == XQueryVersion.VERSION_1_0) {
            XQUERY_1_0_DIALECT = dialect;
        } else if (version == XQueryVersion.VERSION_3_0) {
            XQUERY_3_0_DIALECT = dialect;
        } else if (version == XQueryVersion.VERSION_3_1) {
            XQUERY_3_1_DIALECT = dialect;
        } else {
            throw new AssertionError("Unknown XQuery version: " + version);
        }
    }
}
