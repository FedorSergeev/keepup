package io.keepup.cms.core.service;

public class TestEntityInterfaceImpl implements TestEntityInterface {

    @ContentId
    private Long id;

    @ContentMapping("name")
    private String name;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return null;
    }
}
