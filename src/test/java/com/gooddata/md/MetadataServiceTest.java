/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.md;

import com.gooddata.GoodDataException;
import com.gooddata.GoodDataRestException;
import com.gooddata.gdc.UriResponse;
import com.gooddata.project.Project;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetadataServiceTest {

    private static final String URI = "TEST_URI";
    private static final String ID = "TEST_ID";
    private static final String PROJECT_ID = "TEST_PROJ_ID";

    @Mock
    private Project project;
    @Mock
    private RestTemplate restTemplate;

    private MetadataService service;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        service = new MetadataService(restTemplate);
        when(project.getId()).thenReturn(PROJECT_ID);
    }

    @Test(expectedExceptions = ObjCreateException.class)
    public void testCreateObjNullResponse() throws Exception {
        final Obj obj = mock(Obj.class);
        service.createObj(project, obj);
    }

    @Test(expectedExceptions = ObjCreateException.class)
    @SuppressWarnings("unchecked")
    public void testCreateObjGDRestException() throws Exception {
        final Obj obj = mock(Obj.class);
        when(restTemplate.postForObject(Obj.URI, obj, UriResponse.class, PROJECT_ID))
                .thenThrow(GoodDataRestException.class);
        service.createObj(project, obj);
    }

    @Test(expectedExceptions = ObjCreateException.class)
    public void testCreateObjRestClientException() throws Exception {
        final Obj obj = mock(Obj.class);
        when(restTemplate.postForObject(Obj.URI, obj, UriResponse.class, PROJECT_ID))
                .thenThrow(new RestClientException(""));
        service.createObj(project, obj);
    }

    @Test
    public void testCreateObj() throws Exception {
        final Obj obj = mock(Obj.class);
        final Obj resultObj = mock(Obj.class);
        final UriResponse uriResp = mock(UriResponse.class);

        when(restTemplate.postForObject(Obj.URI, obj, UriResponse.class, PROJECT_ID)).thenReturn(uriResp);
        when(uriResp.getUri()).thenReturn(URI);
        when(restTemplate.getForObject(URI, obj.getClass())).thenReturn(resultObj);

        final Obj result = service.createObj(project, obj);
        assertThat(result, is(notNullValue()));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetObjByUriNullUri() throws Exception {
        service.getObjByUri(null, Obj.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetObjByUriNullCls() throws Exception {
        service.getObjByUri(URI, null);
    }

    @Test(expectedExceptions = ObjNotFoundException.class)
    public void testGetObjByUriNotFound() throws Exception {
        final GoodDataRestException restException = mock(GoodDataRestException.class);
        when(restException.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND.value());
        when(restTemplate.getForObject(URI, Obj.class)).thenThrow(restException);

        service.getObjByUri(URI, Obj.class);
    }

    @Test
    public void testGetObjByUri() throws Exception {
        final Obj resultObj = mock(Obj.class);
        when(restTemplate.getForObject(URI, resultObj.getClass())).thenReturn(resultObj);

        final Obj result = service.getObjByUri(URI, resultObj.getClass());
        assertThat(result, is(resultObj));
    }

    @Test(expectedExceptions = GoodDataException.class)
    public void testGetObjByUriWithClientSideHTTPError() throws Exception {
        when(restTemplate.getForObject(URI, Obj.class)).thenThrow(new RestClientException(""));
        service.getObjByUri(URI, Queryable.class);
    }

    @Test(expectedExceptions = GoodDataRestException.class)
    public void testGetObjByUriWithServerSideHTTPError() throws Exception {
        when(restTemplate.getForObject(URI, Obj.class)).thenThrow(new GoodDataRestException(500, "", "", "", ""));
        service.getObjByUri(URI, Obj.class);
    }

    @Test(expectedExceptions = GoodDataException.class)
    public void testGetObjByUriWithNoResponseFromAPI() throws Exception {
        service.getObjByUri(URI, Obj.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetObjByIdNullProject() throws Exception {
        service.getObjById(null, ID, Obj.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetObjByIdNullId() throws Exception {
        service.getObjById(project, null, Obj.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetObjByIdNullCls() throws Exception {
        service.getObjById(project, ID, null);
    }

    @Test
    public void testGetObjById() throws Exception {
        final Obj resultObj = mock(Obj.class);
        final String uri = format("/gdc/md/%s/obj/%s", PROJECT_ID, ID);
        when(restTemplate.getForObject(uri, resultObj.getClass())).thenReturn(resultObj);

        final Obj result = service.getObjById(project, ID, resultObj.getClass());
        assertThat(result, is(resultObj));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetObjUriNullProject() throws Exception {
        service.getObjUri(null, Queryable.class, Restriction.identifier(""));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetObjUriNullClass() throws Exception {
        service.getObjUri(project, null, Restriction.identifier(""));
    }

    @Test(expectedExceptions = GoodDataException.class)
    public void testGetObjUriNoResponseFromAPI() throws Exception {
        service.getObjUri(project, Queryable.class);
    }

    @Test
    public void testGetObjUriToFindOneObjByTitle() throws Exception {
        final Query queryResult = mock(Query.class);
        final Entry resultEntry = mock(Entry.class);
        final String uri = "myURI";
        final String title = "myTitle";
        when(restTemplate.getForObject(Query.URI, Query.class, project.getId(), "queryable")).thenReturn(queryResult);
        when(queryResult.getEntries()).thenReturn(asList(resultEntry));
        when(resultEntry.getTitle()).thenReturn(title);
        when(resultEntry.getLink()).thenReturn(uri);

        final String result = service.getObjUri(project, Queryable.class, Restriction.title(title));
        assertThat(result, is(uri));
    }

    @Test(expectedExceptions = NonUniqueObjException.class)
    public void testGetObjUriMoreThanOneResult() throws Exception {
        final Query queryResult = mock(Query.class);
        final Entry resultEntry1 = mock(Entry.class);
        final Entry resultEntry2 = mock(Entry.class);
        when(restTemplate.getForObject(Query.URI, Query.class, project.getId(), "queryable")).thenReturn(queryResult);
        when(queryResult.getEntries()).thenReturn(asList(resultEntry1, resultEntry2));

        service.getObjUri(project, Queryable.class);
    }

    @Test(expectedExceptions = ObjNotFoundException.class)
    public void testGetObjUriNothingFound() throws Exception {
        final Query queryResult = mock(Query.class);
        final Entry resultEntry = mock(Entry.class);
        final String title = "myTitle";
        when(restTemplate.getForObject(Query.URI, Query.class, project.getId(), "queryable")).thenReturn(queryResult);
        when(queryResult.getEntries()).thenReturn(asList(resultEntry));

        service.getObjUri(project, Queryable.class, Restriction.title(title));
    }

    @Test
    public void testGetObjToFindOneObjById() throws Exception {
        final Queryable intendedResult = mock(Queryable.class);
        final Query queryResult = mock(Query.class);
        final Entry resultEntry = mock(Entry.class);
        final String uri = "myURI";
        final String id = "myId";
        when(restTemplate.getForObject(Query.URI, Query.class, project.getId(), "queryable")).thenReturn(queryResult);
        when(queryResult.getEntries()).thenReturn(asList(resultEntry));
        when(resultEntry.getIdentifier()).thenReturn(id);
        when(resultEntry.getLink()).thenReturn(uri);
        when(restTemplate.getForObject(uri, Queryable.class)).thenReturn(intendedResult);

        final Queryable result = service.getObj(project, Queryable.class, Restriction.identifier(id));
        assertThat(result, is(intendedResult));
    }

    @Test(expectedExceptions = NonUniqueObjException.class)
    public void testGetObjMoreThanOneResult() throws Exception {
        final Query queryResult = mock(Query.class);
        final Entry resultEntry1 = mock(Entry.class);
        final Entry resultEntry2 = mock(Entry.class);
        when(restTemplate.getForObject(Query.URI, Query.class, project.getId(), "queryable")).thenReturn(queryResult);
        when(queryResult.getEntries()).thenReturn(asList(resultEntry1, resultEntry2));

        service.getObj(project, Queryable.class);
    }

    @Test(expectedExceptions = ObjNotFoundException.class)
    public void testGetObjNothingFound() throws Exception {
        final Query queryResult = mock(Query.class);
        final Entry resultEntry = mock(Entry.class);
        final String title = "myTitle";
        when(restTemplate.getForObject(Query.URI, Query.class, project.getId(), "queryable")).thenReturn(queryResult);
        when(queryResult.getEntries()).thenReturn(asList(resultEntry));

        service.getObj(project, Queryable.class, Restriction.title(title));
    }

    @Test
    public void testFindMoreResults() throws Exception {
        final Query queryResult = mock(Query.class);
        final Entry resultEntry1 = mock(Entry.class);
        final Entry resultEntry2 = mock(Entry.class);
        when(restTemplate.getForObject(Query.URI, Query.class, project.getId(), "queryable")).thenReturn(queryResult);
        when(queryResult.getEntries()).thenReturn(asList(resultEntry1, resultEntry2));

        final Collection<Entry> results = service.find(project, Queryable.class);
        assertThat(results, allOf(hasItem(resultEntry1), hasItem(resultEntry2)));
    }

    @Test(expectedExceptions = GoodDataException.class)
    public void testFindWithWithClientSideHTTPError() throws Exception {
        when(restTemplate.getForObject(Query.URI, Query.class, project.getId(), "queryable")).thenThrow(new RestClientException(""));
        service.find(project, Queryable.class);
    }

    @Test
    public void testFindUrisBySummary() throws Exception {
        final Query queryResult = mock(Query.class);
        final Entry resultEntry1 = mock(Entry.class);
        final Entry resultEntry2 = mock(Entry.class);
        final String summary = "mySummary";
        final String uri1 = "uri1";
        final String uri2 = "uri2";
        when(restTemplate.getForObject(Query.URI, Query.class, project.getId(), "queryable")).thenReturn(queryResult);
        when(queryResult.getEntries()).thenReturn(asList(resultEntry1, resultEntry2));
        when(resultEntry1.getSummary()).thenReturn(summary);
        when(resultEntry2.getSummary()).thenReturn(summary);
        when(resultEntry1.getLink()).thenReturn(uri1);
        when(resultEntry2.getLink()).thenReturn(uri2);

        final Collection<String> results = service.findUris(project, Queryable.class, Restriction.summary(summary));
        assertThat(results, allOf(hasItem(uri1), hasItem(uri2)));
    }

}