package io.vanillabp.spi.service;

/**
 * The default of {@link MultiInstanceElement#resolverBean()}. It means that the annotation names
 * no resolver at all. Nothing implements it, and VanillaBP never looks for a bean of it. VanillaBP
 * only compares the attribute with this class.
 */
public interface NoResolver extends MultiInstanceElementResolver<Object, Object> {

}
