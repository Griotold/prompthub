package com.griotold.prompthub.domain.tag;

public class TagFixture {

    public static Tag createTag() {
        return Tag.create("AI");
    }

    public static Tag createTag(String name) {
        return Tag.create(name);
    }
}