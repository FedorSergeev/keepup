package io.keepup.plugins.catalog.service;

import io.keepup.cms.core.service.ContentId;
import io.keepup.cms.core.service.ContentMapping;
import io.keepup.plugins.catalog.model.CatalogEntity;
import io.keepup.plugins.catalog.model.AttributeType;
import io.keepup.plugins.catalog.model.Layout;
import io.keepup.plugins.catalog.model.LayoutApiAttribute;

import java.util.ArrayList;

public class TestCatalogEntity implements CatalogEntity {

    /**
     * We put it here just for the testing purposes, but we do not mind that layout should be implemented as the object field
     */
    private Layout testLayout;

    @ContentId
    private Long id;
    @ContentMapping("name")
    private String name;
    @ContentMapping("layout_name")
    private String layoutName;
    @ContentMapping("file")
    private String file;

    public TestCatalogEntity() {
        var layoutAttributes = new ArrayList<LayoutApiAttribute>();
        layoutAttributes.add(new LayoutApiAttribute());
        layoutAttributes.get(0).setTag("p");
        layoutAttributes.get(0).setTable(true);
        layoutAttributes.get(0).setResolve(AttributeType.HTML);
        layoutAttributes.get(0).setKey("name");
        layoutAttributes.get(0).setName("Name");
        testLayout = new Layout();
        testLayout.setHtml("<p>{{name}}</p>");
        testLayout.setName("test entity");
        testLayout.setAttributes(layoutAttributes);
        layoutName = testLayout.getName();
    }

    @Override
    public String getLayoutName() {
        return testLayout.getName();
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Layout getTestLayout() {
        return testLayout;
    }

    public void setTestLayout(Layout testLayout) {
        this.testLayout = testLayout;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
