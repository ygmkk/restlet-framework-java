/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.internal.conversion;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.apispark.internal.model.Contact;
import org.restlet.ext.apispark.internal.model.Contract;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.model.PayLoad;
import org.restlet.ext.apispark.internal.model.License;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.ext.apispark.internal.model.Section;
import org.restlet.ext.apispark.internal.model.swagger.ApiDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ApiInfo;
import org.restlet.ext.apispark.internal.model.swagger.AuthorizationsDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.BasicAuthorizationDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ItemsDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ModelDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.OAuth2AuthorizationDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ResourceDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ResourceListing;
import org.restlet.ext.apispark.internal.model.swagger.ResourceOperationDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ResourceOperationParameterDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.ResponseMessageDeclaration;
import org.restlet.ext.apispark.internal.model.swagger.TypePropertyDeclaration;
import org.restlet.ext.apispark.internal.reflect.ReflectUtils;

/**
 * Tool library for converting Restlet Web API Definition to and from Swagger
 * documentation.
 * 
 * @author Cyprien Quilici
 */
public abstract class SwaggerTranslator {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(SwaggerTranslator.class
            .getName());

    /** Supported version of Swagger. */
    private static final String SWAGGER_VERSION = "1.2";

    /**
     * Retrieves the Swagger API declaration corresponding to a category of the
     * given Restlet Web API Definition
     * 
     * @param sectionName
     *            The category of the API declaration
     * @param definition
     *            The Restlet Web API Definition
     * @return The Swagger API definition of the given category
     */
    public static ApiDeclaration getApiDeclaration(String sectionName,
            Definition definition) {
        ApiDeclaration result = new ApiDeclaration();
        result.setApiVersion(definition.getVersion());

        // No way to specify multiple endpoints in Swagger so we take the first
        // one
        Endpoint endpoint;
        if (!definition.getEndpoints().isEmpty()) {
            endpoint = definition.getEndpoints().get(0);
            result.setBasePath(endpoint.computeUrl());
        } else {
            endpoint = new Endpoint("http://example.com");
        }

        // Authentication
        // TODO deal with API key authentication
        AuthorizationsDeclaration authorizations = new AuthorizationsDeclaration();
        if (ChallengeScheme.HTTP_BASIC.getName().equals(
                (endpoint.getAuthenticationProtocol()))) {
            authorizations.setBasicAuth(new BasicAuthorizationDeclaration());
        } else if (ChallengeScheme.HTTP_OAUTH.getName().equals(
                (endpoint.getAuthenticationProtocol()))
                || ChallengeScheme.HTTP_OAUTH_BEARER.getName().equals(
                        (endpoint.getAuthenticationProtocol()))
                || ChallengeScheme.HTTP_OAUTH_MAC.getName().equals(
                        (endpoint.getAuthenticationProtocol()))) {
            authorizations.setOauth2(new OAuth2AuthorizationDeclaration());
        }

        result.setInfo(new ApiInfo());
        result.setSwaggerVersion(SWAGGER_VERSION);
        result.setResourcePath("/" + sectionName);
        Set<String> usedModels = new HashSet<String>();

        Contract contract = definition.getContract();

        // Get sections
        List<Resource> resources = contract.getResources();
        boolean allResources = contract.getSections().isEmpty();
        for (Resource resource : contract.getResources()) {
            if (allResources) {
                resources.add(resource);
            } else {
                if (resource.getSections().contains(sectionName)) {
                    resources.add(resource);
                }
            }
        }

        // Get resources
        for (Resource resource : resources) {
            // Discriminate the resources of one category
            if (!resource.getResourcePath().startsWith("/" + sectionName)) {
                continue;
            }
            ResourceDeclaration rd = new ResourceDeclaration();
            rd.setPath(resource.getResourcePath());
            rd.setDescription(resource.getDescription());

            // Get operations
            for (Operation operation : resource.getOperations()) {
                ResourceOperationDeclaration rod = new ResourceOperationDeclaration();
                rod.setMethod(operation.getMethod());
                rod.setSummary(operation.getDescription());
                rod.setNickname(operation.getName());
                rod.setProduces(operation.getProduces());
                rod.setConsumes(operation.getConsumes());

                // Get path variables
                ResourceOperationParameterDeclaration ropd;
                for (PathVariable pv : resource.getPathVariables()) {
                    ropd = new ResourceOperationParameterDeclaration();
                    ropd.setParamType("path");
                    ropd.setType(toSwaggerType(pv.getType()));
                    ropd.setRequired(true);
                    ropd.setName(pv.getName());
                    ropd.setAllowMultiple(false);
                    ropd.setDescription(pv.getDescription());
                    rod.getParameters().add(ropd);
                }

                // Get in representation
                PayLoad inRepr = operation.getInputPayLoad();
                if (inRepr != null) {
                    Representation representation = definition.getContract()
                            .getRepresentation(inRepr.getType());

                    ropd = new ResourceOperationParameterDeclaration();
                    ropd.setParamType("body");
                    ropd.setRequired(true);

                    if (representation != null && representation.isRaw()) {
                        ropd.setType("File");
                    } else {
                        ropd.setType(toSwaggerType(inRepr.getType()));
                    }
                    if (inRepr.getType() != null) {
                        usedModels.add(inRepr.getType());
                    }
                    rod.getParameters().add(ropd);
                }

                // Get out representation
                PayLoad outRepr = null;
                for (Response response : operation.getResponses()) {
                    if (Status.isSuccess(response.getCode())) {
                        outRepr = response.getOutputPayLoad();
                    }
                }
                if (outRepr != null && outRepr.getType() != null) {
                    if (outRepr.isArray()) {
                        rod.setType("array");
                        if (isPrimitiveType(outRepr.getType())) {
                            rod.getItems().setType(
                                    toSwaggerType(outRepr.getType()));
                        } else {
                            rod.getItems().setRef(outRepr.getType());
                        }
                    } else {
                        rod.setType(toSwaggerType(outRepr.getType()));
                    }
                    usedModels.add(outRepr.getType());
                } else {
                    rod.setType("void");
                }

                // Get query parameters
                for (QueryParameter qp : operation.getQueryParameters()) {
                    ropd = new ResourceOperationParameterDeclaration();
                    ropd.setParamType("query");
                    ropd.setType(toSwaggerType(qp.getType()));
                    ropd.setName(qp.getName());
                    ropd.setAllowMultiple(true);
                    ropd.setDescription(qp.getDescription());
                    ropd.setEnum_(qp.getEnumeration());
                    ropd.setDefaultValue(qp.getDefaultValue());
                    rod.getParameters().add(ropd);
                }

                // Get response messages
                for (Response response : operation.getResponses()) {
                    if (Status.isSuccess(response.getCode())) {
                        continue;
                    }
                    ResponseMessageDeclaration rmd = new ResponseMessageDeclaration();
                    rmd.setCode(response.getCode());
                    rmd.setMessage(response.getMessage());
                    if (response.getOutputPayLoad() != null) {
                        rmd.setResponseModel(response.getOutputPayLoad()
                                .getType());
                    }
                    rod.getResponseMessages().add(rmd);
                }

                rd.getOperations().add(rod);
            }
            result.getApis().add(rd);
        }

        result.setModels(new TreeMap<String, ModelDeclaration>());
        Iterator<String> iterator = usedModels.iterator();
        while (iterator.hasNext()) {
            String model = iterator.next();
            Representation repr = contract.getRepresentation(model);
            if (repr == null || isPrimitiveType(model)) {
                continue;
            }
            ModelDeclaration md = new ModelDeclaration();
            md.setId(model);
            md.setDescription(repr.getDescription());
            for (Property prop : repr.getProperties()) {
                if (prop.getMinOccurs() > 0) {
                    md.getRequired().add(prop.getName());
                }
                if (!isPrimitiveType(prop.getType())
                        && !usedModels.contains(prop.getType())) {
                    usedModels.add(prop.getType());
                    iterator = usedModels.iterator();
                }
                TypePropertyDeclaration tpd = new TypePropertyDeclaration();
                tpd.setDescription(prop.getDescription());
                tpd.setEnum_(prop.getEnumeration());

                if (prop.getMaxOccurs() > 1 || prop.getMaxOccurs() == -1) {
                    tpd.setType("array");
                    tpd.setItems(new ItemsDeclaration());
                    if (isPrimitiveType(prop.getType())) {
                        tpd.getItems().setType(toSwaggerType(prop.getType()));
                    } else {
                        tpd.getItems().setRef(prop.getType());
                    }
                } else {
                    if (isPrimitiveType(prop.getType())) {
                        tpd.setType(toSwaggerType(prop.getType()));
                    } else {
                        tpd.setRef(prop.getType());
                    }
                }
                tpd.setMaximum(prop.getMax());
                tpd.setMinimum(prop.getMin());
                tpd.setUniqueItems(prop.isUniqueItems());

                md.getProperties().put(prop.getName(), tpd);
            }
            result.getModels().put(md.getId(), md);
        }

        // Sort the API declarations according to their path.
        Collections.sort(result.getApis(),
                new Comparator<ResourceDeclaration>() {
                    @Override
                    public int compare(ResourceDeclaration o1,
                            ResourceDeclaration o2) {
                        return o1.getPath().compareTo(o2.getPath());
                    }
                });
        return result;
    }

    /**
     * Translates a Restlet Web API Definition to a Swagger resource listing.
     * 
     * @param definition
     *            The Restlet Web API Definition.
     * @return The corresponding resource listing
     */
    public static ResourceListing getResourcelisting(Definition definition) {
        ResourceListing result = new ResourceListing();

        // common properties
        result.setApiVersion(definition.getVersion());
        // result.setBasePath(definition.getEndpoint());
        result.setInfo(new ApiInfo());
        result.setSwaggerVersion(SWAGGER_VERSION);
        if (definition.getContact() != null) {
            result.getInfo().setContact(definition.getContact().getEmail());
        }
        if (definition.getLicense() != null) {
            result.getInfo().setLicenseUrl(definition.getLicense().getUrl());
        }
        if (definition.getContract() != null) {
            result.getInfo().setTitle(definition.getContract().getName());
            result.getInfo().setDescription(
                    definition.getContract().getDescription());
        }

        Contract contract = definition.getContract();
        boolean allResources = contract.getSections().isEmpty();

        // Resources
        List<String> addedApis = new ArrayList<String>();
        if (definition.getContract() != null && contract.getResources() != null) {
            result.setApis(new ArrayList<ResourceDeclaration>());

            for (Resource resource : contract.getResources()) {
                ResourceDeclaration rd = new ResourceDeclaration();

                if (allResources) {
                    rd.setDescription(resource.getDescription());
                    rd.setPath(ReflectUtils.getFirstSegment(resource
                            .getResourcePath()));
                    if (!addedApis.contains(rd.getPath())) {
                        addedApis.add(rd.getPath());
                        result.getApis().add(rd);
                    }
                } else {
                    for (String sectionName : resource.getSections()) {
                        Section section = contract.getSection(sectionName);
                        rd = new ResourceDeclaration();
                        rd.setDescription(section.getDescription());
                        rd.setPath("/" + sectionName);
                        if (!addedApis.contains(rd.getPath())) {
                            addedApis.add(rd.getPath());
                            result.getApis().add(rd);
                        }
                    }
                }
            }
        }
        Collections.sort(result.getApis(),
                new Comparator<ResourceDeclaration>() {
                    @Override
                    public int compare(ResourceDeclaration o1,
                            ResourceDeclaration o2) {
                        return o1.getPath().compareTo(o2.getPath());
                    }

                });
        return result;
    }

    /**
     * Indicates if the given type is a primitive type.
     * 
     * @param type
     *            The type to be analysed
     * @return A boolean of value true if the given type is primitive, false
     *         otherwise.
     */
    private static boolean isPrimitiveType(String type) {
        if ("string".equals(type.toLowerCase())
                || "int".equals(type.toLowerCase())
                || "integer".equals(type.toLowerCase())
                || "long".equals(type.toLowerCase())
                || "float".equals(type.toLowerCase())
                || "double".equals(type.toLowerCase())
                || "date".equals(type.toLowerCase())
                || "boolean".equals(type.toLowerCase())
                || "bool".equals(type.toLowerCase())) {
            return true;
        }
        return false;
    }

    /**
     * Converts a Swagger parameter to an instance of {@link PayLoad}.
     * 
     * @param parameter
     *            The Swagger parameter.
     * @return An instance of {@link PayLoad}.
     */
    private static PayLoad toEntity(
            ResourceOperationParameterDeclaration parameter) {
        PayLoad result = new PayLoad();
        if ("array".equals(parameter.getType())) {
            result.setArray(true);
            if (parameter.getItems() != null
                    && parameter.getItems().getType() != null) {
                result.setType(parameter.getItems().getType());
            } else if (parameter.getItems() != null) {
                result.setType(parameter.getItems().getRef());
            }
        } else {
            result.setArray(false);
            result.setType(parameter.getType());
        }
        return result;
    }

    /**
     * Converts a Swagger parameter to an instance of {@link PathVariable}.
     * 
     * @param parameter
     *            The Swagger parameter.
     * @return An instance of {@link PathVariable}.
     */
    private static PathVariable toPathVariable(
            ResourceOperationParameterDeclaration parameter) {
        PathVariable result = new PathVariable();
        result.setName(parameter.getName());
        result.setDescription(parameter.getDescription());
        result.setType(toRwadefType(parameter.getType()));
        result.setArray(parameter.isAllowMultiple());
        return result;
    }

    /**
     * Converts a Swagger parameter to an instance of {@link QueryParameter}.
     * 
     * @param parameter
     *            The Swagger parameter.
     * @return An instance of {@link QueryParameter}.
     */
    private static QueryParameter toQueryParameter(
            ResourceOperationParameterDeclaration parameter) {
        QueryParameter result = new QueryParameter();
        result.setName(parameter.getName());
        result.setDescription(parameter.getDescription());
        result.setRequired(parameter.isRequired());
        result.setAllowMultiple(parameter.isAllowMultiple());
        result.setDefaultValue(parameter.getDefaultValue());
        if (parameter.getEnum_() != null && !parameter.getEnum_().isEmpty()) {
            result.setEnumeration(new ArrayList<String>());
            for (String value : parameter.getEnum_()) {
                result.getEnumeration().add(value);
            }
        }
        return result;
    }

    /**
     * Converts a Swagger model to an instance of {@link Representation}.
     * 
     * @param model
     *            The Swagger model.
     * @param name
     *            The name of the representation.
     * @return An instance of {@link Representation}.
     */
    private static Representation toRepresentation(ModelDeclaration model,
            String name) {
        Representation result = new Representation();
        result.setName(name);
        result.setDescription(model.getDescription());

        // Set properties
        for (Entry<String, TypePropertyDeclaration> swagProperties : model
                .getProperties().entrySet()) {
            TypePropertyDeclaration swagProperty = swagProperties.getValue();
            Property property = new Property();
            property.setName(swagProperties.getKey());

            // Set property's type
            boolean isArray = "array".equals(swagProperty.getType());
            if (isArray) {
                property.setType(swagProperty.getItems().getType() != null ? swagProperty
                        .getItems().getType() : swagProperty.getItems()
                        .getRef());
            } else if (swagProperty.getType() != null) {
                property.setType(swagProperty.getType());
            } else if (swagProperty.getRef() != null) {
                property.setType(swagProperty.getRef());
            }

            if (model.getRequired() != null) {
                property.setMinOccurs(model.getRequired().contains(
                        swagProperties.getKey()) ? 1 : 0);
            } else {
                property.setMinOccurs(0);
            }
            property.setMaxOccurs(isArray ? -1 : 1);
            property.setDescription(swagProperty.getDescription());
            property.setMin(swagProperty.getMinimum());
            property.setMax(swagProperty.getMaximum());
            property.setUniqueItems(swagProperty.isUniqueItems());

            result.getProperties().add(property);
            LOGGER.log(Level.FINE, "Property " + property.getName() + " added.");
        }
        return result;
    }

    /**
     * Returns the primitive types as RWADef expects them
     * 
     * @param type
     *            The type name to Swaggerize
     * @return The Swaggerized type
     */
    private static String toRwadefType(String type) {
        if ("int".equals(type)) {
            return "Integer";
        } else if ("string".equals(type)) {
            return "String";
        } else if ("boolean".equals(type)) {
            return "Boolean";
        } else {
            return type;
        }
    }

    /**
     * Returns the primitive types as Swagger expects them
     * 
     * @param type
     *            The type name to Swaggerize
     * @return The Swaggerized type
     */
    private static String toSwaggerType(String type) {
        if ("Integer".equals(type)) {
            return "int";
        } else if ("String".equals(type)) {
            return "string";
        } else if ("Boolean".equals(type)) {
            return "boolean";
        } else {
            return type;
        }
    }

    /**
     * Translates a Swagger documentation to a Restlet definition.
     * 
     * @param resourceListing
     *            The Swagger resource listing.
     * @param apiDeclarations
     *            The list of Swagger API declarations.
     * @return The Restlet definition.
     * @throws TranslationException
     */
    public static Definition translate(ResourceListing resourceListing,
            Map<String, ApiDeclaration> apiDeclarations)
            throws TranslationException {

        validate(resourceListing, apiDeclarations);

        boolean containsRawTypes = false;
        List<String> declaredTypes = new ArrayList<String>();
        List<String> declaredPathVariables;
        Map<String, List<String>> subtypes = new HashMap<String, List<String>>();

        try {
            Definition definition = new Definition();
            definition.setVersion(resourceListing.getApiVersion());
            Contact contact = new Contact();
            contact.setEmail(resourceListing.getInfo().getContact());
            definition.setContact(contact);
            License license = new License();
            license.setUrl(resourceListing.getInfo().getLicenseUrl());
            definition.setLicense(license);

            Contract contract = new Contract();
            contract.setName(resourceListing.getInfo().getTitle());
            LOGGER.log(Level.FINE, "Contract " + contract.getName() + " added.");
            contract.setDescription(resourceListing.getInfo().getDescription());
            definition.setContract(contract);

            // Resource listing
            Resource resource;
            for (Entry<String, ApiDeclaration> entry : apiDeclarations
                    .entrySet()) {
                ApiDeclaration swagApiDeclaration = entry.getValue();
                List<String> apiProduces = swagApiDeclaration.getProduces();
                List<String> apiConsumes = swagApiDeclaration.getConsumes();
                Section section = new Section();
                section.setName(entry.getKey());
                section.setDescription(resourceListing.getApi(
                        "/" + entry.getKey()).getDescription());

                for (ResourceDeclaration api : swagApiDeclaration.getApis()) {
                    declaredPathVariables = new ArrayList<String>();
                    resource = new Resource();
                    resource.setResourcePath(api.getPath());

                    // Operations listing
                    Operation operation;
                    for (ResourceOperationDeclaration swagOperation : api
                            .getOperations()) {
                        String methodName = swagOperation.getMethod();
                        operation = new Operation();
                        operation.setMethod(swagOperation.getMethod());
                        operation.setName(swagOperation.getNickname());
                        operation.setDescription(swagOperation.getSummary());

                        // Set variants
                        Representation representation;
                        for (String produced : apiProduces.isEmpty() ? swagOperation
                                .getProduces() : apiProduces) {
                            if (!containsRawTypes
                                    && MediaType.MULTIPART_FORM_DATA.getName()
                                            .equals(produced)) {
                                representation = new Representation();
                                representation.setName("File");
                                representation.setRaw(true);
                                containsRawTypes = true;
                                representation.getSections().add(
                                        section.getName());
                                contract.getRepresentations().add(
                                        representation);
                            }
                            operation.getProduces().add(produced);
                        }
                        for (String consumed : apiConsumes.isEmpty() ? swagOperation
                                .getConsumes() : apiConsumes) {
                            if (!containsRawTypes
                                    && MediaType.MULTIPART_FORM_DATA.getName()
                                            .equals(consumed)) {
                                representation = new Representation();
                                representation.setName("File");
                                representation.setRaw(true);
                                containsRawTypes = true;
                                representation.getSections().add(
                                        section.getName());
                                contract.getRepresentations().add(
                                        representation);
                            }
                            operation.getConsumes().add(consumed);
                        }

                        // Set response's entity
                        PayLoad rwadOutRepr = new PayLoad();
                        if ("array".equals(swagOperation.getType())) {
                            LOGGER.log(Level.FINER, "Operation: "
                                    + swagOperation.getNickname()
                                    + " returns an array");
                            rwadOutRepr.setArray(true);
                            if (swagOperation.getItems().getType() != null) {
                                rwadOutRepr.setType(swagOperation.getItems()
                                        .getType());
                            } else {
                                rwadOutRepr.setType(swagOperation.getItems()
                                        .getRef());
                            }
                        } else {
                            LOGGER.log(Level.FINER, "Operation: "
                                    + swagOperation.getNickname()
                                    + " returns a single Representation");
                            rwadOutRepr.setArray(false);
                            if (swagOperation.getType() != null) {
                                rwadOutRepr.setType(swagOperation.getType());
                            } else {
                                rwadOutRepr.setType(swagOperation.getRef());
                            }
                        }

                        // Extract success response message
                        Response success = new Response();
                        success.setCode(Status.SUCCESS_OK.getCode());
                        success.setOutputPayLoad(rwadOutRepr);
                        success.setDescription("Success");
                        success.setMessage(Status.SUCCESS_OK.getDescription());
                        success.setName("Success");
                        operation.getResponses().add(success);

                        // Loop over Swagger parameters.
                        for (ResourceOperationParameterDeclaration param : swagOperation
                                .getParameters()) {
                            if ("path".equals(param.getParamType())) {
                                if (!declaredPathVariables.contains(param
                                        .getName())) {
                                    declaredPathVariables.add(param.getName());
                                    PathVariable pathVariable = toPathVariable(param);
                                    resource.getPathVariables().add(
                                            pathVariable);
                                }
                            } else if ("body".equals(param.getParamType())) {
                                if (operation.getInputPayLoad() == null) {
                                    PayLoad rwadInRepr = toEntity(param);
                                    operation.setInputPayLoad(rwadInRepr);
                                }
                            } else if ("query".equals(param.getParamType())) {
                                QueryParameter rwadQueryParam = toQueryParameter(param);
                                operation.getQueryParameters().add(
                                        rwadQueryParam);
                            }
                        }

                        // Set error response messages
                        if (swagOperation.getResponseMessages() != null) {
                            for (ResponseMessageDeclaration swagResponse : swagOperation
                                    .getResponseMessages()) {
                                Response response = new Response();
                                PayLoad outputPayLoad = new PayLoad();
                                outputPayLoad.setType(swagResponse
                                        .getResponseModel());
                                response.setOutputPayLoad(outputPayLoad);
                                response.setName("Error "
                                        + swagResponse.getCode());
                                response.setCode(swagResponse.getCode());
                                response.setMessage(swagResponse.getMessage());
                                operation.getResponses().add(response);
                            }
                        }

                        resource.getOperations().add(operation);
                        LOGGER.log(Level.FINE, "Method " + methodName
                                + " added.");

                        // Add representations
                        for (Entry<String, ModelDeclaration> modelEntry : swagApiDeclaration
                                .getModels().entrySet()) {
                            ModelDeclaration model = modelEntry.getValue();
                            if (model.getSubTypes() != null
                                    && !model.getSubTypes().isEmpty()) {
                                subtypes.put(model.getId(), model.getSubTypes());
                            }
                            if (!declaredTypes.contains(modelEntry.getKey())) {
                                declaredTypes.add(modelEntry.getKey());
                                representation = toRepresentation(model,
                                        modelEntry.getKey());
                                representation.getSections().add(
                                        section.getName());
                                contract.getRepresentations().add(
                                        representation);
                                LOGGER.log(Level.FINE, "Representation "
                                        + modelEntry.getKey() + " added.");
                            }
                        }

                        // Deal with subtyping
                        for (Entry<String, List<String>> subtypesPair : subtypes
                                .entrySet()) {
                            List<String> subtypesOf = subtypesPair.getValue();
                            for (String subtypeOf : subtypesOf) {
                                representation = contract
                                        .getRepresentation(subtypeOf);
                                representation.setExtendedType(subtypesPair
                                        .getKey());
                            }
                        }
                    }

                    resource.getSections().add(section.getName());
                    contract.getResources().add(resource);
                    LOGGER.log(Level.FINE, "Resource " + api.getPath()
                            + " added.");
                }

                if (definition.getEndpoints().isEmpty()) {
                    // TODO verify how to deal with API key auth + oauth
                    Endpoint endpoint = new Endpoint(
                            swagApiDeclaration.getBasePath());
                    definition.getEndpoints().add(endpoint);
                    if (resourceListing.getAuthorizations().getBasicAuth() != null) {
                        endpoint.setAuthenticationProtocol(ChallengeScheme.HTTP_BASIC
                                .getName());
                    } else if (resourceListing.getAuthorizations().getOauth2() != null) {
                        endpoint.setAuthenticationProtocol(ChallengeScheme.HTTP_OAUTH
                                .getName());
                    } else if (resourceListing.getAuthorizations().getApiKey() != null) {
                        endpoint.setAuthenticationProtocol(ChallengeScheme.CUSTOM
                                .getName());
                    }
                }
            }
            LOGGER.log(Level.FINE,
                    "Definition successfully retrieved from Swagger definition");
            return definition;
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                throw new TranslationException("file",
                        ((FileNotFoundException) e).getMessage());
            } else {
                throw new TranslationException("compliance",
                        "Impossible to read your API definition, check your Swagger specs compliance");
            }
        }
    }

    /**
     * Indicates if the given resource listing and list of API declarations
     * match.
     * 
     * @param resourceListing
     *            The Swagger resource listing.
     * @param apiDeclarations
     *            The list of Swagger API declarations.
     * @throws TranslationException
     */
    private static void validate(ResourceListing resourceListing,
            Map<String, ApiDeclaration> apiDeclarations)
            throws TranslationException {
        int rlSize = resourceListing.getApis().size();
        int adSize = apiDeclarations.size();
        if (rlSize < adSize) {
            throw new TranslationException("file",
                    "One of your API declarations is not mapped in your resource listing");
        } else if (rlSize > adSize) {
            throw new TranslationException("file",
                    "Some API declarations are missing");
        }
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private SwaggerTranslator() {
    }
}