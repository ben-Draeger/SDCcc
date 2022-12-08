/*
 * This Source Code Form is subject to the terms of the MIT License.
 * Copyright (c) 2022 Draegerwerk AG & Co. KGaA.
 *
 * SPDX-License-Identifier: MIT
 */

package com.draeger.medical.sdccc.manipulation.precondition.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.draeger.medical.sdccc.manipulation.Manipulations;
import com.draeger.medical.sdccc.sdcri.testclient.TestClient;
import com.draeger.medical.sdccc.tests.test_util.InjectorUtil;
import com.draeger.medical.sdccc.util.TestRunObserver;
import com.draeger.medical.t2iapi.ResponseTypes;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.somda.sdc.biceps.common.MdibEntity;
import org.somda.sdc.biceps.model.participant.AlertActivation;
import org.somda.sdc.biceps.model.participant.AlertSystemDescriptor;
import org.somda.sdc.biceps.model.participant.AlertSystemState;
import org.somda.sdc.biceps.model.participant.ContextAssociation;
import org.somda.sdc.biceps.model.participant.LocationContextDescriptor;
import org.somda.sdc.biceps.model.participant.LocationContextState;
import org.somda.sdc.biceps.model.participant.PatientContextDescriptor;
import org.somda.sdc.biceps.model.participant.PatientContextState;
import org.somda.sdc.glue.consumer.SdcRemoteDevice;

/**
 * Unit tests for manipulation preconditions in {@linkplain ManipulationPreconditions}.
 */
public class ManipulationPreconditionsTest {

    private static final String PATIENT_CONTEXT_DESCRIPTOR_HANDLE = "patpatpatpat";
    private static final String PATIENT_CONTEXT_STATE_HANDLE = "patpatstate";
    private static final String PATIENT_CONTEXT_STATE_HANDLE2 = "patpatstate2";
    private static final String LOCATION_CONTEXT_DESCRIPTOR_HANDLE = "locloclocloc";
    private static final String LOCATION_CONTEXT_STATE_HANDLE = "loclocstate";
    private static final String LOCATION_CONTEXT_STATE_HANDLE2 = "loclocstate2";
    private static final String ALERT_SYSTEM_CONTEXT_HANDLE = "alerthandle";
    private static final String ALERT_SYSTEM_CONTEXT_HANDLE2 = "alerthandle2";

    private Injector injector;
    private SdcRemoteDevice mockDevice;
    private Manipulations mockManipulations;
    private PatientContextState mockPatientContextState;
    private PatientContextState mockPatientContextState2;
    private LocationContextState mockLocationContextState;
    private LocationContextState mockLocationContextState2;
    private AlertSystemState mockAlertSystemState;
    private AlertSystemState mockAlertSystemState2;
    private TestRunObserver testRunObserver;
    private MdibEntity mockEntity;
    private MdibEntity mockEntity2;

    @BeforeEach
    void setUp() throws IOException {
        mockDevice = mock(SdcRemoteDevice.class, Mockito.RETURNS_DEEP_STUBS);
        mockManipulations = mock(Manipulations.class);
        mockPatientContextState = mock(PatientContextState.class);
        mockPatientContextState2 = mock(PatientContextState.class);
        mockLocationContextState = mock(LocationContextState.class);
        mockLocationContextState2 = mock(LocationContextState.class);
        mockAlertSystemState = mock(AlertSystemState.class);
        mockAlertSystemState2 = mock(AlertSystemState.class);
        mockEntity = mock(MdibEntity.class);
        mockEntity2 = mock(MdibEntity.class);

        final var mockTestClient = mock(TestClient.class);
        when(mockTestClient.getSdcRemoteDevice()).thenReturn(mockDevice);

        injector = InjectorUtil.setupInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(TestClient.class).toInstance(mockTestClient);
                bind(SdcRemoteDevice.class).toInstance(mockDevice);
                bind(Manipulations.class).toInstance(mockManipulations);
            }
        });

        testRunObserver = injector.getInstance(TestRunObserver.class);
    }

    void associateNewPatientsSetup() {
        // create mock patient context state
        when(mockPatientContextState.getDescriptorHandle()).thenReturn(PATIENT_CONTEXT_DESCRIPTOR_HANDLE);
        when(mockPatientContextState.getContextAssociation()).thenReturn(ContextAssociation.ASSOC);
        when(mockPatientContextState.getHandle()).thenReturn(PATIENT_CONTEXT_STATE_HANDLE);

        // create another mock patient context state
        when(mockPatientContextState2.getDescriptorHandle()).thenReturn(PATIENT_CONTEXT_DESCRIPTOR_HANDLE);
        when(mockPatientContextState2.getContextAssociation()).thenReturn(ContextAssociation.ASSOC);
        when(mockPatientContextState2.getHandle()).thenReturn(PATIENT_CONTEXT_STATE_HANDLE2);

        // make manipulation return our two patient context state handles and nothing afterwards
        when(mockManipulations.createContextStateWithAssociation(
                        PATIENT_CONTEXT_DESCRIPTOR_HANDLE, ContextAssociation.ASSOC))
                .thenReturn(Optional.of(PATIENT_CONTEXT_STATE_HANDLE))
                .thenReturn(Optional.of(PATIENT_CONTEXT_STATE_HANDLE2))
                .thenReturn(Optional.empty());

        // return mock states on request
        when(mockDevice.getMdibAccess().getState(PATIENT_CONTEXT_STATE_HANDLE, PatientContextState.class))
                .thenReturn(Optional.of(mockPatientContextState));
        when(mockDevice.getMdibAccess().getState(PATIENT_CONTEXT_STATE_HANDLE2, PatientContextState.class))
                .thenReturn(Optional.of(mockPatientContextState2));

        // create mock entity to hold the descriptor
        when(mockEntity.getHandle()).thenReturn(PATIENT_CONTEXT_DESCRIPTOR_HANDLE);
        when(mockDevice.getMdibAccess().findEntitiesByType(PatientContextDescriptor.class))
                .thenReturn(List.of(mockEntity));
    }

    void associateNewLocationsSetup() {
        // create mock location context state
        when(mockLocationContextState.getDescriptorHandle()).thenReturn(LOCATION_CONTEXT_DESCRIPTOR_HANDLE);
        when(mockLocationContextState.getContextAssociation()).thenReturn(ContextAssociation.ASSOC);
        when(mockLocationContextState.getHandle()).thenReturn(LOCATION_CONTEXT_STATE_HANDLE);

        // create another mock location context state
        when(mockLocationContextState2.getDescriptorHandle()).thenReturn(LOCATION_CONTEXT_DESCRIPTOR_HANDLE);
        when(mockLocationContextState2.getContextAssociation()).thenReturn(ContextAssociation.ASSOC);
        when(mockLocationContextState2.getHandle()).thenReturn(LOCATION_CONTEXT_STATE_HANDLE2);

        // make manipulation return our two location context state handles and nothing afterwards
        when(mockManipulations.createContextStateWithAssociation(
                        LOCATION_CONTEXT_DESCRIPTOR_HANDLE, ContextAssociation.ASSOC))
                .thenReturn(Optional.of(LOCATION_CONTEXT_STATE_HANDLE))
                .thenReturn(Optional.of(LOCATION_CONTEXT_STATE_HANDLE2))
                .thenReturn(Optional.empty());

        // return mock states on request
        when(mockDevice.getMdibAccess().getState(LOCATION_CONTEXT_STATE_HANDLE, LocationContextState.class))
                .thenReturn(Optional.of(mockLocationContextState));
        when(mockDevice.getMdibAccess().getState(LOCATION_CONTEXT_STATE_HANDLE2, LocationContextState.class))
                .thenReturn(Optional.of(mockLocationContextState2));

        // create mock entity to hold the descriptor
        when(mockEntity.getHandle()).thenReturn(LOCATION_CONTEXT_DESCRIPTOR_HANDLE);
        when(mockDevice.getMdibAccess().findEntitiesByType(LocationContextDescriptor.class))
                .thenReturn(List.of(mockEntity));
    }

    @Test
    @DisplayName("associateNewPatients: Associate new patient correctly")
    void testAssociateNewPatientForHandle() {
        associateNewPatientsSetup();
        final var expectedManipulationCalls = 2;

        assertTrue(
                ManipulationPreconditions.AssociatePatientsManipulation.manipulation(injector),
                "Manipulation should've succeeded");
        assertFalse(
                testRunObserver.isInvalid(),
                "Test run should not have been invalid. Reason(s): " + testRunObserver.getReasons());

        final var handleCaptor = ArgumentCaptor.forClass(String.class);
        final var assocCaptor = ArgumentCaptor.forClass(ContextAssociation.class);
        verify(mockManipulations, times(expectedManipulationCalls))
                .createContextStateWithAssociation(handleCaptor.capture(), assocCaptor.capture());

        assertEquals(PATIENT_CONTEXT_DESCRIPTOR_HANDLE, handleCaptor.getValue());
        assertEquals(ContextAssociation.ASSOC, assocCaptor.getValue());
    }

    @Test
    @DisplayName("associateNewPatients: New patient not associated")
    void testAssociateNewPatientForHandleWrongAssociation() {
        associateNewPatientsSetup();

        // introduce error, context won't be associated
        when(mockPatientContextState.getContextAssociation()).thenReturn(ContextAssociation.DIS);

        assertFalse(
                ManipulationPreconditions.AssociatePatientsManipulation.manipulation(injector),
                "manipulation should've failed.");
        assertTrue(testRunObserver.isInvalid(), "Test run should have been invalid.");
    }

    @Test
    @DisplayName("associateNewPatients: New patient wrong descriptor")
    void testAssociateNewPatientForHandleWrongDescriptor() {
        associateNewPatientsSetup();

        final var wrongDescriptorHandle = "mostindeededly";

        // introduce error, state points to wrong descriptor
        when(mockPatientContextState.getDescriptorHandle()).thenReturn(wrongDescriptorHandle);

        assertFalse(
                ManipulationPreconditions.AssociatePatientsManipulation.manipulation(injector),
                "manipulation should've failed.");
        assertTrue(testRunObserver.isInvalid(), "Test run should have been invalid.");
    }

    @Test
    @DisplayName("associateNewPatients: Already existent state")
    void testAssociateNewPatientForHandleUsedState() {
        associateNewPatientsSetup();

        // introduce error, first state handle already in entity
        when(mockEntity.getStates(PatientContextState.class)).thenReturn(List.of(mockPatientContextState));
        assertFalse(
                ManipulationPreconditions.AssociatePatientsManipulation.manipulation(injector),
                "manipulation should've failed.");
        assertTrue(testRunObserver.isInvalid(), "Test run should have been invalid.");
    }

    @Test
    @DisplayName("associateNewPatients: Manipulation returns same handle twice")
    void testAssociateNewPatientForHandleSameStateTwice() {
        associateNewPatientsSetup();

        // introduce error, manipulation returns same handle twice
        when(mockManipulations.createContextStateWithAssociation(
                        PATIENT_CONTEXT_DESCRIPTOR_HANDLE, ContextAssociation.ASSOC))
                .thenReturn(Optional.of(PATIENT_CONTEXT_STATE_HANDLE))
                .thenReturn(Optional.of(PATIENT_CONTEXT_STATE_HANDLE))
                .thenReturn(Optional.empty());

        assertFalse(
                ManipulationPreconditions.AssociatePatientsManipulation.manipulation(injector),
                "manipulation should've failed.");
        assertTrue(testRunObserver.isInvalid(), "Test run should have been invalid.");
    }

    @Test
    @DisplayName("associateNewLocations: Associate new location correctly")
    void testAssociateNewLocationForHandle() {
        associateNewLocationsSetup();
        final var expectedManipulationCalls = 2;

        assertTrue(
                ManipulationPreconditions.AssociateLocationsManipulation.manipulation(injector),
                "Manipulation should've succeeded");
        assertFalse(
                testRunObserver.isInvalid(),
                "Test run should not have been invalid. Reason(s): " + testRunObserver.getReasons());

        final var handleCaptor = ArgumentCaptor.forClass(String.class);
        final var assocCaptor = ArgumentCaptor.forClass(ContextAssociation.class);
        verify(mockManipulations, times(expectedManipulationCalls))
                .createContextStateWithAssociation(handleCaptor.capture(), assocCaptor.capture());

        assertEquals(LOCATION_CONTEXT_DESCRIPTOR_HANDLE, handleCaptor.getValue());
        assertEquals(ContextAssociation.ASSOC, assocCaptor.getValue());
    }

    @Test
    @DisplayName("associateNewLocations: New location not associated")
    void testAssociateNewLocationForHandleWrongAssociation() {
        associateNewLocationsSetup();

        // introduce error, context won't be associated
        when(mockLocationContextState.getContextAssociation()).thenReturn(ContextAssociation.DIS);

        assertFalse(
                ManipulationPreconditions.AssociateLocationsManipulation.manipulation(injector),
                "manipulation should've failed.");
        assertTrue(testRunObserver.isInvalid(), "Test run should have been invalid.");
    }

    @Test
    @DisplayName("associateNewLocations: New location wrong descriptor")
    void testAssociateNewLocationForHandleWrongDescriptor() {
        associateNewLocationsSetup();

        final var wrongDescriptorHandle = "mostindeededly";

        // introduce error, state points to wrong descriptor
        when(mockLocationContextState.getDescriptorHandle()).thenReturn(wrongDescriptorHandle);

        assertFalse(
                ManipulationPreconditions.AssociateLocationsManipulation.manipulation(injector),
                "manipulation should've failed.");
        assertTrue(testRunObserver.isInvalid(), "Test run should have been invalid.");
    }

    @Test
    @DisplayName("associateNewLocations: Already existent state")
    void testAssociateNewLocationForHandleUsedState() {
        associateNewLocationsSetup();

        // introduce error, first state handle already in entity
        when(mockEntity.getStates(LocationContextState.class)).thenReturn(List.of(mockLocationContextState));
        assertFalse(
                ManipulationPreconditions.AssociateLocationsManipulation.manipulation(injector),
                "manipulation should've failed.");
        assertTrue(testRunObserver.isInvalid(), "Test run should have been invalid.");
    }

    @Test
    @DisplayName("associateNewLocations: Manipulation returns same handle twice")
    void testAssociateNewLocationForHandleSameStateTwice() {
        associateNewLocationsSetup();

        // introduce error, manipulation returns same handle twice
        when(mockManipulations.createContextStateWithAssociation(
                        LOCATION_CONTEXT_DESCRIPTOR_HANDLE, ContextAssociation.ASSOC))
                .thenReturn(Optional.of(LOCATION_CONTEXT_STATE_HANDLE))
                .thenReturn(Optional.of(LOCATION_CONTEXT_STATE_HANDLE))
                .thenReturn(Optional.empty());

        assertFalse(
                ManipulationPreconditions.AssociateLocationsManipulation.manipulation(injector),
                "manipulation should've failed.");
        assertTrue(testRunObserver.isInvalid(), "Test run should have been invalid.");
    }

    void setActivationStateSetup() {
        // create mock alert system state
        when(mockAlertSystemState.getDescriptorHandle()).thenReturn(ALERT_SYSTEM_CONTEXT_HANDLE);
        when(mockAlertSystemState.getActivationState())
                .thenReturn(AlertActivation.ON)
                .thenReturn(AlertActivation.PSD)
                .thenReturn(AlertActivation.OFF);

        // create second mock alert system state
        when(mockAlertSystemState2.getDescriptorHandle()).thenReturn(ALERT_SYSTEM_CONTEXT_HANDLE2);
        when(mockAlertSystemState2.getActivationState())
                .thenReturn(AlertActivation.ON)
                .thenReturn(AlertActivation.PSD)
                .thenReturn(AlertActivation.OFF);

        // make manipulation return true for the manipulations and false afterwards
        when(mockManipulations.setAlertActivation(any(String.class), any(AlertActivation.class)))
                .thenReturn(ResponseTypes.Result.RESULT_SUCCESS)
                .thenReturn(ResponseTypes.Result.RESULT_SUCCESS)
                .thenReturn(ResponseTypes.Result.RESULT_SUCCESS)
                .thenReturn(ResponseTypes.Result.RESULT_SUCCESS)
                .thenReturn(ResponseTypes.Result.RESULT_SUCCESS)
                .thenReturn(ResponseTypes.Result.RESULT_SUCCESS)
                .thenReturn(ResponseTypes.Result.RESULT_FAIL);

        // return mock states on request
        when(mockDevice.getMdibAccess().getState(ALERT_SYSTEM_CONTEXT_HANDLE, AlertSystemState.class))
                .thenReturn(Optional.of(mockAlertSystemState));
        when(mockDevice.getMdibAccess().getState(ALERT_SYSTEM_CONTEXT_HANDLE2, AlertSystemState.class))
                .thenReturn(Optional.of(mockAlertSystemState2));

        // create mock entities to hold the states
        when(mockEntity.getHandle()).thenReturn(ALERT_SYSTEM_CONTEXT_HANDLE);
        when(mockEntity2.getHandle()).thenReturn(ALERT_SYSTEM_CONTEXT_HANDLE2);
        when(mockDevice.getMdibAccess().findEntitiesByType(AlertSystemDescriptor.class))
                .thenReturn(List.of(mockEntity, mockEntity2));
    }

    @Test
    @DisplayName("setActivationState: set activation state for an alert system correctly")
    void testSetActivationStateForAlertSystem() {
        setActivationStateSetup();
        final var expectedManipulationCalls = 6;
        final var expectedActivationStates = List.of(
                AlertActivation.ON,
                AlertActivation.PSD,
                AlertActivation.OFF,
                AlertActivation.ON,
                AlertActivation.PSD,
                AlertActivation.OFF);
        final var expectedHandles = List.of(
                ALERT_SYSTEM_CONTEXT_HANDLE,
                ALERT_SYSTEM_CONTEXT_HANDLE,
                ALERT_SYSTEM_CONTEXT_HANDLE,
                ALERT_SYSTEM_CONTEXT_HANDLE2,
                ALERT_SYSTEM_CONTEXT_HANDLE2,
                ALERT_SYSTEM_CONTEXT_HANDLE2);

        assertTrue(
                ManipulationPreconditions.AlertSystemActivationStateManipulation.manipulation(injector),
                "Manipulation should've succeeded");
        assertFalse(
                testRunObserver.isInvalid(),
                "Test run should not have been invalid. Reason(s): " + testRunObserver.getReasons());

        final var handleCaptor = ArgumentCaptor.forClass(String.class);
        final var activationStateCaptor = ArgumentCaptor.forClass(AlertActivation.class);
        verify(mockManipulations, times(expectedManipulationCalls))
                .setAlertActivation(handleCaptor.capture(), activationStateCaptor.capture());

        assertEquals(expectedHandles, handleCaptor.getAllValues());
        assertEquals(expectedActivationStates, activationStateCaptor.getAllValues());
    }

    @Test
    @DisplayName("setActivationState: wrong ActivationState")
    void testSetActivationStateForAlertSystemWrongActivationState() {
        setActivationStateSetup();

        when(mockAlertSystemState.getActivationState()).thenReturn(AlertActivation.OFF);

        assertFalse(
                ManipulationPreconditions.AlertSystemActivationStateManipulation.manipulation(injector),
                "manipulation should've failed.");
        assertTrue(testRunObserver.isInvalid(), "Test run should have been invalid.");
    }
}