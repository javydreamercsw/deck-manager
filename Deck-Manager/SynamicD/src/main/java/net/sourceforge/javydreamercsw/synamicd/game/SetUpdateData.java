package net.sourceforge.javydreamercsw.synamicd.game;

import com.reflexit.magiccards.core.model.Editions;

public class SetUpdateData {

    private String name;
    private String url;
    private Editions.Edition edition;
    private long pagesInSet;

    public SetUpdateData(String name, String url, Editions.Edition edition, long pagesInSet) {
        this.name = name;
        this.url = url;
        this.edition = edition;
        this.pagesInSet = pagesInSet;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the edition
     */
    public Editions.Edition getEdition() {
        return edition;
    }

    /**
     * @param edition the edition to set
     */
    public void setEdition(Editions.Edition edition) {
        this.edition = edition;
    }

    /**
     * @return the pagesInSet
     */
    public long getPagesInSet() {
        return pagesInSet;
    }

    /**
     * @param pagesInSet the pagesInSet to set
     */
    public void setPagesInSet(long pagesInSet) {
        this.pagesInSet = pagesInSet;
    }
}