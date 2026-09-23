package io.vanillabp.spi.service;

/**
 * The default of {@link MultiInstanceElement#resolverBean()}, which is how that annotation says
 * that it names no resolver at all. Nothing implements it and VanillaBP never looks for a bean of
 * it: the attribute is compared against this very class.
 */
public interface NoResolver extends MultiInstanceElementResolver<Object, Object> {

}
