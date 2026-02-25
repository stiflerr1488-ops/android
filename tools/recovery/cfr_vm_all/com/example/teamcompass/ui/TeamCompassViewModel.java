/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  android.app.Application
 *  android.content.Context
 *  android.hardware.Sensor
 *  android.hardware.SensorManager
 *  android.hardware.display.DisplayManager
 *  android.location.Location
 *  android.media.ToneGenerator
 *  android.net.Uri
 *  android.os.Bundle
 *  android.os.VibrationEffect
 *  android.os.Vibrator
 *  android.util.Log
 *  android.view.Window
 *  androidx.annotation.StringRes
 *  androidx.annotation.VisibleForTesting
 *  androidx.compose.runtime.internal.StabilityInferred
 *  androidx.core.content.ContextCompat
 *  androidx.lifecycle.AndroidViewModel
 *  androidx.lifecycle.SavedStateHandle
 *  androidx.lifecycle.ViewModel
 *  androidx.lifecycle.ViewModelKt
 *  com.example.teamcompass.R$string
 *  com.example.teamcompass.core.CompassTarget
 *  com.example.teamcompass.core.LocationPoint
 *  com.example.teamcompass.core.PlayerMode
 *  com.example.teamcompass.core.PlayerState
 *  com.example.teamcompass.core.PrioritizedTarget
 *  com.example.teamcompass.core.TargetFilterPreset
 *  com.example.teamcompass.core.TargetFilterState
 *  com.example.teamcompass.core.TrackingMode
 *  com.google.android.gms.location.FusedLocationProviderClient
 *  com.google.android.gms.location.LocationServices
 *  com.google.firebase.analytics.FirebaseAnalytics
 *  com.google.firebase.auth.FirebaseAuth
 *  dagger.hilt.android.lifecycle.HiltViewModel
 *  javax.inject.Inject
 *  kotlin.Lazy
 *  kotlin.LazyKt
 *  kotlin.Metadata
 *  kotlin.NoWhenBranchMatchedException
 *  kotlin.Pair
 *  kotlin.Result
 *  kotlin.ResultKt
 *  kotlin.TuplesKt
 *  kotlin.Unit
 *  kotlin.collections.CollectionsKt
 *  kotlin.collections.MapsKt
 *  kotlin.coroutines.Continuation
 *  kotlin.coroutines.CoroutineContext
 *  kotlin.coroutines.intrinsics.IntrinsicsKt
 *  kotlin.coroutines.jvm.internal.Boxing
 *  kotlin.coroutines.jvm.internal.ContinuationImpl
 *  kotlin.jvm.functions.Function0
 *  kotlin.jvm.functions.Function1
 *  kotlin.jvm.functions.Function2
 *  kotlin.jvm.functions.Function3
 *  kotlin.jvm.functions.Function4
 *  kotlin.jvm.functions.Function6
 *  kotlin.jvm.internal.DefaultConstructorMarker
 *  kotlin.jvm.internal.Intrinsics
 *  kotlin.jvm.internal.Ref$BooleanRef
 *  kotlin.jvm.internal.Ref$IntRef
 *  kotlin.jvm.internal.Ref$ObjectRef
 *  kotlin.jvm.internal.SourceDebugExtension
 *  kotlin.ranges.RangesKt
 *  kotlin.sequences.Sequence
 *  kotlin.sequences.SequencesKt
 *  kotlin.text.StringsKt
 *  kotlinx.coroutines.BuildersKt
 *  kotlinx.coroutines.CoroutineExceptionHandler
 *  kotlinx.coroutines.CoroutineExceptionHandler$Key
 *  kotlinx.coroutines.CoroutineScope
 *  kotlinx.coroutines.DelayKt
 *  kotlinx.coroutines.Job
 *  kotlinx.coroutines.Job$DefaultImpls
 *  kotlinx.coroutines.flow.Flow
 *  kotlinx.coroutines.flow.FlowCollector
 *  kotlinx.coroutines.flow.FlowKt
 *  kotlinx.coroutines.flow.MutableSharedFlow
 *  kotlinx.coroutines.flow.MutableStateFlow
 *  kotlinx.coroutines.flow.SharedFlow
 *  kotlinx.coroutines.flow.SharedFlowKt
 *  kotlinx.coroutines.flow.StateFlow
 *  kotlinx.coroutines.flow.StateFlowKt
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.example.teamcompass.ui;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.location.Location;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Window;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.compose.runtime.internal.StabilityInferred;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import com.example.teamcompass.R;
import com.example.teamcompass.auth.FirebaseIdentityLinkingService;
import com.example.teamcompass.auth.IdentityLinkingEligibility;
import com.example.teamcompass.auth.IdentityLinkingService;
import com.example.teamcompass.auth.NoOpIdentityLinkingService;
import com.example.teamcompass.core.CompassTarget;
import com.example.teamcompass.core.LocationPoint;
import com.example.teamcompass.core.PlayerMode;
import com.example.teamcompass.core.PlayerState;
import com.example.teamcompass.core.PrioritizedTarget;
import com.example.teamcompass.core.TargetFilterPreset;
import com.example.teamcompass.core.TargetFilterState;
import com.example.teamcompass.core.TrackingMode;
import com.example.teamcompass.domain.TeamActionError;
import com.example.teamcompass.domain.TeamActionFailure;
import com.example.teamcompass.domain.TeamActionResult;
import com.example.teamcompass.domain.TeamEnemyPing;
import com.example.teamcompass.domain.TeamMemberPrefs;
import com.example.teamcompass.domain.TeamMemberRoleProfile;
import com.example.teamcompass.domain.TeamPoint;
import com.example.teamcompass.domain.TeamRepository;
import com.example.teamcompass.domain.TeamRolePatch;
import com.example.teamcompass.domain.TeamSnapshot;
import com.example.teamcompass.domain.TeamViewMode;
import com.example.teamcompass.domain.TrackingController;
import com.example.teamcompass.domain.TrackingTelemetry;
import com.example.teamcompass.p2p.P2PInboundMessage;
import com.example.teamcompass.p2p.P2PTransportManager;
import com.example.teamcompass.perf.TeamCompassPerfMetrics;
import com.example.teamcompass.perf.TeamCompassPerfSnapshot;
import com.example.teamcompass.ui.ActionTraceIdProvider;
import com.example.teamcompass.ui.AlertsCoordinator;
import com.example.teamcompass.ui.AuthDelegate;
import com.example.teamcompass.ui.AuthState;
import com.example.teamcompass.ui.AutoBrightnessBinding;
import com.example.teamcompass.ui.BackendHealthDelegate;
import com.example.teamcompass.ui.BackendHealthMonitor;
import com.example.teamcompass.ui.BluetoothScanCoordinator;
import com.example.teamcompass.ui.CompassControlId;
import com.example.teamcompass.ui.CompassControlLayoutKt;
import com.example.teamcompass.ui.ControlLayoutPreset;
import com.example.teamcompass.ui.ControlPosition;
import com.example.teamcompass.ui.CrashlyticsStructuredLogger;
import com.example.teamcompass.ui.EnemyPing;
import com.example.teamcompass.ui.EventNotificationManager;
import com.example.teamcompass.ui.FilterUiState;
import com.example.teamcompass.ui.FirebaseAuthGateway;
import com.example.teamcompass.ui.HeadingSensorCoordinator;
import com.example.teamcompass.ui.JoinRateLimiter;
import com.example.teamcompass.ui.KmlPoint;
import com.example.teamcompass.ui.LocationReadinessCoordinator;
import com.example.teamcompass.ui.LocationReadinessUpdate;
import com.example.teamcompass.ui.MapCoordinator;
import com.example.teamcompass.ui.MapUiState;
import com.example.teamcompass.ui.MemberPrefsSyncJobs;
import com.example.teamcompass.ui.MemberPrefsSyncWorker;
import com.example.teamcompass.ui.QuickCommandType;
import com.example.teamcompass.ui.SessionCoordinator;
import com.example.teamcompass.ui.SettingsUiState;
import com.example.teamcompass.ui.StructuredLogger;
import com.example.teamcompass.ui.TacticalActionsCoordinator;
import com.example.teamcompass.ui.TacticalMap;
import com.example.teamcompass.ui.TargetFilterCoordinator;
import com.example.teamcompass.ui.TeamActionErrorPolicy;
import com.example.teamcompass.ui.TeamCompassViewModel;
import com.example.teamcompass.ui.TeamCompassViewModel$bindSavedStateHandle$1$invokeSuspend$;
import com.example.teamcompass.ui.TeamCompassViewModel$startMemberPrefsSync$;
import com.example.teamcompass.ui.TeamListeningPreflightResult;
import com.example.teamcompass.ui.TeamMarkerMappersKt;
import com.example.teamcompass.ui.TeamSessionDelegate;
import com.example.teamcompass.ui.TeamSnapshotObserver;
import com.example.teamcompass.ui.TeamUiState;
import com.example.teamcompass.ui.TelemetryState;
import com.example.teamcompass.ui.TrackingCoordinator;
import com.example.teamcompass.ui.TrackingUiState;
import com.example.teamcompass.ui.UiEvent;
import com.example.teamcompass.ui.UiState;
import com.example.teamcompass.ui.UnifiedMarker;
import com.example.teamcompass.ui.UserPrefs;
import com.example.teamcompass.ui.UuidActionTraceIdProvider;
import com.example.teamcompass.ui.theme.ThemeMode;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import javax.inject.Inject;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.Metadata;
import kotlin.NoWhenBranchMatchedException;
import kotlin.Pair;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.functions.Function4;
import kotlin.jvm.functions.Function6;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.ranges.RangesKt;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt;
import kotlin.text.StringsKt;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineExceptionHandler;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.DelayKt;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowCollector;
import kotlinx.coroutines.flow.FlowKt;
import kotlinx.coroutines.flow.MutableSharedFlow;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.SharedFlow;
import kotlinx.coroutines.flow.SharedFlowKt;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(mv={2, 0, 0}, k=1, xi=48, d1={"\u0000\u00f8\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\u0010\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010\u0003\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u0006\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u0011\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u000b\b\u0007\u0018\u0000 \u00b4\u00022\u00020\u0001:\u0004\u00b4\u0002\u00b5\u0002B\u00aa\u0001\b\u0000\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0011\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0013\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0015\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0017\u0012+\b\u0002\u0010\u0018\u001a%\b\u0001\u0012\u0004\u0012\u00020\u0000\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001b0\u001a\u0012\u0006\u0012\u0004\u0018\u00010\u001c\u0018\u00010\u0019\u00a2\u0006\u0002\b\u001d\u0012\n\b\u0002\u0010\u001e\u001a\u0004\u0018\u00010\u001f\u00a2\u0006\u0004\b \u0010!BI\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\u0012\u001a\u00020\u0013\u0012\u0006\u0010\u0016\u001a\u00020\u0017\u0012\u0006\u0010\u001e\u001a\u00020\u001f\u00a2\u0006\u0004\b \u0010\"J\u0010\u0010~\u001a\u00020R2\u0006\u0010\u007f\u001a\u00020RH\u0002J4\u0010\u0080\u0001\u001a\u00020\u001b2\u0006\u0010\u007f\u001a\u00020R2\u0007\u0010\u0081\u0001\u001a\u00020R2\u000b\b\u0002\u0010\u0082\u0001\u001a\u0004\u0018\u00010R2\u000b\b\u0002\u0010\u0083\u0001\u001a\u0004\u0018\u00010RH\u0002J4\u0010\u0084\u0001\u001a\u00020\u001b2\u0006\u0010\u007f\u001a\u00020R2\u0007\u0010\u0081\u0001\u001a\u00020R2\u000b\b\u0002\u0010\u0082\u0001\u001a\u0004\u0018\u00010R2\u000b\b\u0002\u0010\u0083\u0001\u001a\u0004\u0018\u00010RH\u0002JO\u0010\u0085\u0001\u001a\u00020\u001b2\u0006\u0010\u007f\u001a\u00020R2\u0007\u0010\u0081\u0001\u001a\u00020R2\f\b\u0002\u0010\u0086\u0001\u001a\u0005\u0018\u00010\u0087\u00012\u000b\b\u0002\u0010\u0088\u0001\u001a\u0004\u0018\u00010R2\u000b\b\u0002\u0010\u0082\u0001\u001a\u0004\u0018\u00010R2\u000b\b\u0002\u0010\u0083\u0001\u001a\u0004\u0018\u00010RH\u0002J\t\u0010\u0089\u0001\u001a\u00020\u001bH\u0002J%\u0010\u008a\u0001\u001a\u00020\u001b2\u0014\u0010\u008b\u0001\u001a\u000f\u0012\u0004\u0012\u00020a\u0012\u0004\u0012\u00020a0\u008c\u0001H\u0001\u00a2\u0006\u0003\b\u008d\u0001J\u0018\u0010\u008e\u0001\u001a\u00020\u001b2\u0007\u0010\u008f\u0001\u001a\u00020^H\u0001\u00a2\u0006\u0003\b\u0090\u0001J\u001e\u0010\u0091\u0001\u001a\u00020\u001b2\u0007\u0010\u0088\u0001\u001a\u00020R2\f\b\u0002\u0010\u0092\u0001\u001a\u0005\u0018\u00010\u0087\u0001J\t\u0010\u0093\u0001\u001a\u00020\u001bH\u0002J\t\u0010\u0094\u0001\u001a\u00020\u001bH\u0002J\u0007\u0010\u0095\u0001\u001a\u00020\u001bJ\u0014\u0010\u0096\u0001\u001a\u00020\u001b2\t\u0010\u0083\u0001\u001a\u0004\u0018\u00010RH\u0002J\u0010\u0010\u0097\u0001\u001a\u00020\u001b2\u0007\u0010\u0098\u0001\u001a\u00020RJ\u0010\u0010\u0099\u0001\u001a\u00020\u001b2\u0007\u0010\u009a\u0001\u001a\u00020YJ\u0011\u0010\u009b\u0001\u001a\u00020\u001b2\b\u0010\u009a\u0001\u001a\u00030\u009c\u0001J\u001b\u0010\u009d\u0001\u001a\u00020\u001b2\b\u0010\u009e\u0001\u001a\u00030\u009f\u00012\b\u0010\u00a0\u0001\u001a\u00030\u009f\u0001J\u001b\u0010\u00a1\u0001\u001a\u00020\u001b2\b\u0010\u009e\u0001\u001a\u00030\u009f\u00012\b\u0010\u00a0\u0001\u001a\u00030\u009f\u0001J\u0011\u0010\u00a2\u0001\u001a\u00020\u001b2\b\u0010\u009a\u0001\u001a\u00030\u00a3\u0001J\u0010\u0010\u00a4\u0001\u001a\u00020\u001b2\u0007\u0010\u00a5\u0001\u001a\u00020\rJ\u0007\u0010\u00a6\u0001\u001a\u00020\u001bJ\t\u0010\u00a7\u0001\u001a\u00020\u001bH\u0002J\u0007\u0010\u00a8\u0001\u001a\u00020\u001bJ\u0007\u0010\u00a9\u0001\u001a\u00020\u001bJ\u0010\u0010\u00aa\u0001\u001a\u00020\u001b2\u0007\u0010\u009a\u0001\u001a\u00020[J\u0010\u0010\u00ab\u0001\u001a\u00020\u001b2\u0007\u0010\u00ac\u0001\u001a\u00020\rJ\u0011\u0010\u00ad\u0001\u001a\u00020\u001b2\b\u0010\u00ae\u0001\u001a\u00030\u00af\u0001J\u0011\u0010\u00b0\u0001\u001a\u00020\u001b2\b\u0010\u00b1\u0001\u001a\u00030\u009f\u0001J\u0010\u0010\u00b2\u0001\u001a\u00020\u001b2\u0007\u0010\u00b3\u0001\u001a\u00020\rJ\u0010\u0010\u00b4\u0001\u001a\u00020\u001b2\u0007\u0010\u00b5\u0001\u001a\u00020\rJ\u0010\u0010\u00b6\u0001\u001a\u00020\u001b2\u0007\u0010\u00ac\u0001\u001a\u00020\rJ!\u0010\u00b7\u0001\u001a\u00020\u001b2\u0016\u0010\u008b\u0001\u001a\u0011\u0012\u0005\u0012\u00030\u00b8\u0001\u0012\u0005\u0012\u00030\u00b8\u00010\u008c\u0001H\u0002J\u0011\u0010\u00b9\u0001\u001a\u00020\u001b2\b\u0010\u00ba\u0001\u001a\u00030\u00bb\u0001J\u0007\u0010\u00bc\u0001\u001a\u00020\u001bJ\u0010\u0010\u00bd\u0001\u001a\u00020\u001b2\u0007\u0010\u00ac\u0001\u001a\u00020\rJ\u0011\u0010\u00be\u0001\u001a\u00020\u001b2\b\u0010\u00bf\u0001\u001a\u00030\u00c0\u0001J+\u0010\u00c1\u0001\u001a\u00020\u001b2\u000f\u0010\u00c2\u0001\u001a\n\u0012\u0005\u0012\u00030\u00c4\u00010\u00c3\u00012\u0011\b\u0002\u0010\u00c5\u0001\u001a\n\u0012\u0005\u0012\u00030\u00c4\u00010\u00c3\u0001J5\u0010\u00c6\u0001\u001a\u00020\u001b2\b\u0010\u00ba\u0001\u001a\u00030\u00bb\u00012\u000f\u0010\u00c2\u0001\u001a\n\u0012\u0005\u0012\u00030\u00c4\u00010\u00c3\u00012\u0011\b\u0002\u0010\u00c5\u0001\u001a\n\u0012\u0005\u0012\u00030\u00c4\u00010\u00c3\u0001J?\u0010\u00c7\u0001\u001a\u00020\u001b2\b\u0010\u00c8\u0001\u001a\u00030\u00bb\u00012\b\u0010\u00c9\u0001\u001a\u00030\u00ca\u00012\u000f\u0010\u00c2\u0001\u001a\n\u0012\u0005\u0012\u00030\u00c4\u00010\u00c3\u00012\u000f\u0010\u00c5\u0001\u001a\n\u0012\u0005\u0012\u00030\u00c4\u00010\u00c3\u0001H\u0002J\u0007\u0010\u00cb\u0001\u001a\u00020\u001bJ\u0007\u0010\u00cc\u0001\u001a\u00020\u001bJ\u0007\u0010\u00cd\u0001\u001a\u00020\u001bJ\"\u0010\u00ce\u0001\u001a\u00020\u001b2\u0007\u0010\u00cf\u0001\u001a\u00020R2\u0007\u0010\u00d0\u0001\u001a\u00020R2\u0007\u0010\u00d1\u0001\u001a\u00020\rJ6\u0010\u00d2\u0001\u001a\u00020\u001b2\b\u0010\u00d3\u0001\u001a\u00030\u00d4\u00012\b\u0010\u00d5\u0001\u001a\u00030\u00d4\u00012\u0007\u0010\u00cf\u0001\u001a\u00020R2\u0007\u0010\u00d0\u0001\u001a\u00020R2\u0007\u0010\u00d1\u0001\u001a\u00020\rJ?\u0010\u00d6\u0001\u001a\u00020\u001b2\u0007\u0010\u00d7\u0001\u001a\u00020R2\b\u0010\u00d3\u0001\u001a\u00030\u00d4\u00012\b\u0010\u00d5\u0001\u001a\u00030\u00d4\u00012\u0007\u0010\u00cf\u0001\u001a\u00020R2\u0007\u0010\u00d0\u0001\u001a\u00020R2\u0007\u0010\u00d8\u0001\u001a\u00020\rJ\u0019\u0010\u00d9\u0001\u001a\u00020\u001b2\u0007\u0010\u00d7\u0001\u001a\u00020R2\u0007\u0010\u00d8\u0001\u001a\u00020\rJ\u0011\u0010\u00da\u0001\u001a\u00020\u001b2\b\u0010\u00db\u0001\u001a\u00030\u00dc\u0001J%\u0010\u00dd\u0001\u001a\u00020\u001b2\b\u0010\u00d3\u0001\u001a\u00030\u00d4\u00012\b\u0010\u00d5\u0001\u001a\u00030\u00d4\u00012\b\u0010\u00db\u0001\u001a\u00030\u00dc\u0001J\u001a\u0010\u00de\u0001\u001a\u00020\u001b2\u0007\u0010\u00df\u0001\u001a\u00020R2\b\u0010\u00e0\u0001\u001a\u00030\u00e1\u0001J!\u0010\u00e2\u0001\u001a\u00020\u001b2\u000e\u0010\u00e3\u0001\u001a\t\u0012\u0004\u0012\u00020R0\u00c3\u00012\b\u0010\u00e0\u0001\u001a\u00030\u00e1\u0001J\u0007\u0010\u00e4\u0001\u001a\u00020\u001bJ\u001b\u0010\u00e5\u0001\u001a\u00020\u001b2\u0007\u0010\u00e6\u0001\u001a\u00020R2\t\b\u0002\u0010\u00e7\u0001\u001a\u00020\rJ\u0019\u0010\u00e8\u0001\u001a\u00020\u001b2\u0007\u0010\u00e9\u0001\u001a\u00020RH\u0082@\u00a2\u0006\u0003\u0010\u00ea\u0001J\u0012\u0010\u00eb\u0001\u001a\u00020\u001b2\u0007\u0010\u0082\u0001\u001a\u00020RH\u0002J\u0007\u0010\u00ec\u0001\u001a\u00020\u001bJ\u0007\u0010\u00ed\u0001\u001a\u00020\u001bJ\u0010\u0010\u00ee\u0001\u001a\u00020\u001b2\u0007\u0010\u00ac\u0001\u001a\u00020\rJ\u001b\u0010\u00ef\u0001\u001a\u00020\u001b2\b\u0010\u00d7\u0001\u001a\u00030\u00f0\u00012\b\u0010\u00f1\u0001\u001a\u00030\u00f2\u0001J\u0007\u0010\u00f3\u0001\u001a\u00020\u001bJ\u0011\u0010\u00f4\u0001\u001a\u00020\u001b2\b\u0010\u00ae\u0001\u001a\u00030\u00f5\u0001J\u0007\u0010\u00f6\u0001\u001a\u00020\u001bJ\u0012\u0010\u00f7\u0001\u001a\u00020\u001b2\u0007\u0010\u00e6\u0001\u001a\u00020RH\u0002J.\u0010\u00f8\u0001\u001a\u00020\r2\b\u0010\u00f9\u0001\u001a\u00030\u00fa\u00012\u0007\u0010\u0082\u0001\u001a\u00020R2\u0007\u0010\u0083\u0001\u001a\u00020R2\u0007\u0010\u0081\u0001\u001a\u00020RH\u0002J\t\u0010\u00fb\u0001\u001a\u00020\u001bH\u0002J\"\u0010\u00fc\u0001\u001a\u00020\u001b2\u0007\u0010\u0082\u0001\u001a\u00020R2\u0007\u0010\u0083\u0001\u001a\u00020RH\u0082@\u00a2\u0006\u0003\u0010\u00fd\u0001J\t\u0010\u00fe\u0001\u001a\u00020\u001bH\u0002J\u001b\u0010\u00ff\u0001\u001a\u00020\u001b2\u0007\u0010\u0082\u0001\u001a\u00020R2\u0007\u0010\u0080\u0002\u001a\u00020RH\u0002J\u001c\u0010\u0081\u0002\u001a\u00020\u001b2\b\u0010\u0082\u0002\u001a\u00030\u0083\u00022\u0007\u0010\u0080\u0002\u001a\u00020RH\u0002J\t\u0010\u0084\u0002\u001a\u00020\u001bH\u0002J\t\u0010\u0085\u0002\u001a\u00020\u001bH\u0002J\u0014\u0010\u0086\u0002\u001a\u00020\u001b2\t\b\u0002\u0010\u008f\u0001\u001a\u00020^H\u0002J\u0014\u0010\u0087\u0002\u001a\u00020\u001b2\t\b\u0002\u0010\u008f\u0001\u001a\u00020^H\u0002J\u001b\u0010\u0088\u0002\u001a\u00020\r2\u0007\u0010\u0089\u0002\u001a\u00020^2\u0007\u0010\u008f\u0001\u001a\u00020^H\u0002J\u001b\u0010\u008a\u0002\u001a\u00020\u001b2\u0007\u0010\u0082\u0001\u001a\u00020R2\u0007\u0010\u0083\u0001\u001a\u00020RH\u0002J\u001a\u0010\u008b\u0002\u001a\u00020\u001b2\u000f\u0010\u008c\u0002\u001a\n\u0012\u0005\u0012\u00030\u008d\u00020\u00c3\u0001H\u0002J\t\u0010\u008e\u0002\u001a\u00020\u001bH\u0002J\u0012\u0010\u008f\u0002\u001a\u00020\u001b2\u0007\u0010\u0090\u0002\u001a\u00020\rH\u0002J\t\u0010\u0091\u0002\u001a\u00020\u001bH\u0002J\t\u0010\u0092\u0002\u001a\u00020\u001bH\u0002J\u001b\u0010\u0093\u0002\u001a\u00020\u001b2\u0007\u0010\u009a\u0001\u001a\u00020Y2\t\b\u0002\u0010\u0094\u0002\u001a\u00020\rJ\t\u0010\u0095\u0002\u001a\u00020\u001bH\u0002J\u0007\u0010\u0096\u0002\u001a\u00020\u001bJ\u0007\u0010\u0097\u0002\u001a\u00020\u001bJ\u0018\u0010\u0098\u0002\u001a\n\u0012\u0005\u0012\u00030\u0099\u00020\u00c3\u00012\u0007\u0010\u008f\u0001\u001a\u00020^J\u0014\u0010\u009a\u0002\u001a\u00020\u001b2\t\b\u0002\u0010\u008f\u0001\u001a\u00020^H\u0002J\t\u0010\u009b\u0002\u001a\u00020\u001bH\u0002J\t\u0010\u009c\u0002\u001a\u00020\u001bH\u0002J\t\u0010\u009d\u0002\u001a\u00020\u001bH\u0002J\u0016\u0010\u009e\u0002\u001a\u0004\u0018\u00010R2\t\u0010\u009f\u0002\u001a\u0004\u0018\u00010RH\u0002J1\u0010\u00a0\u0002\u001a\u00020R2\n\b\u0001\u0010\u00a1\u0002\u001a\u00030\u009f\u00012\u0014\u0010\u00a2\u0002\u001a\u000b\u0012\u0006\b\u0001\u0012\u00020\u001c0\u00a3\u0002\"\u00020\u001cH\u0002\u00a2\u0006\u0003\u0010\u00a4\u0002J\u001c\u0010\u00a5\u0002\u001a\u00020\u001b2\u0007\u0010\u00a6\u0002\u001a\u00020R2\b\u0010\u00f9\u0001\u001a\u00030\u00fa\u0001H\u0002J\u0007\u0010\u00a7\u0002\u001a\u00020\u001bJ\t\u0010\u00a8\u0002\u001a\u00020\u001bH\u0014J\u0013\u0010\u00a9\u0002\u001a\u00020\u001b2\n\u0010\u00aa\u0002\u001a\u0005\u0018\u00010\u00ab\u0002J\u0010\u0010\u00ac\u0002\u001a\u00020\u001b2\u0007\u0010\u00ac\u0001\u001a\u00020\rJ\u0011\u0010\u00ad\u0002\u001a\u00020\u001b2\b\u0010\u00ae\u0002\u001a\u00030\u00c0\u0001J\u0010\u0010\u00af\u0002\u001a\u00020\u001b2\u0007\u0010\u0098\u0001\u001a\u00020\rJ\u0007\u0010\u00b0\u0002\u001a\u00020\u001bJ\u0007\u0010\u00b1\u0002\u001a\u00020\rJ\u0007\u0010\u00b2\u0002\u001a\u00020\u001bJ\u0007\u0010\u00b3\u0002\u001a\u00020\u001bR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R3\u0010\u0018\u001a%\b\u0001\u0012\u0004\u0012\u00020\u0000\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001b0\u001a\u0012\u0006\u0012\u0004\u0018\u00010\u001c\u0018\u00010\u0019\u00a2\u0006\u0002\b\u001dX\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010#R\u0010\u0010\u001e\u001a\u0004\u0018\u00010\u001fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010$\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020&X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0018\u0010'\u001a\n )*\u0004\u0018\u00010(0(X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010*R\u000e\u0010+\u001a\u00020,X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010-\u001a\u00020\rX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010.\u001a\u00020/X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u00100\u001a\u000201X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u00102\u001a\u000203X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u00104\u001a\u000205X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u00106\u001a\u000207X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u00108\u001a\u000209X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010:\u001a\u00020;X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010<\u001a\u00020=X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010>\u001a\u00020?X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010@\u001a\u00020AX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010B\u001a\u00020CX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010D\u001a\u00020EX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010F\u001a\u00020GX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010H\u001a\u00020IX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010J\u001a\u00020KX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010L\u001a\u0004\u0018\u00010MX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010N\u001a\u0004\u0018\u00010MX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010O\u001a\u0004\u0018\u00010MX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010P\u001a\u0004\u0018\u00010MX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010Q\u001a\u0004\u0018\u00010RX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010S\u001a\u0004\u0018\u00010MX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010T\u001a\u0004\u0018\u00010MX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010U\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010V\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010W\u001a\u0004\u0018\u00010RX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010X\u001a\u00020YX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010Z\u001a\u00020[X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\\\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010]\u001a\u00020^X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010_\u001a\b\u0012\u0004\u0012\u00020a0`X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010b\u001a\b\u0012\u0004\u0012\u00020a0c\u00a2\u0006\b\n\u0000\u001a\u0004\bd\u0010eR\u0014\u0010f\u001a\b\u0012\u0004\u0012\u00020h0gX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010i\u001a\b\u0012\u0004\u0012\u00020h0j\u00a2\u0006\b\n\u0000\u001a\u0004\bk\u0010lR\u001b\u0010m\u001a\u00020n8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\bq\u0010r\u001a\u0004\bo\u0010pR\u0014\u0010s\u001a\b\u0012\u0004\u0012\u00020u0tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010v\u001a\u00020u8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\bw\u0010xR\u001b\u0010y\u001a\u00020z8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b}\u0010r\u001a\u0004\b{\u0010|\u00a8\u0006\u00b6\u0002"}, d2={"Lcom/example/teamcompass/ui/TeamCompassViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "app", "Landroid/app/Application;", "teamRepository", "Lcom/example/teamcompass/domain/TeamRepository;", "trackingController", "Lcom/example/teamcompass/domain/TrackingController;", "prefs", "Lcom/example/teamcompass/ui/UserPrefs;", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "autoStart", "", "actionTraceIdProvider", "Lcom/example/teamcompass/ui/ActionTraceIdProvider;", "structuredLogger", "Lcom/example/teamcompass/ui/StructuredLogger;", "coroutineExceptionHandler", "Lkotlinx/coroutines/CoroutineExceptionHandler;", "identityLinkingService", "Lcom/example/teamcompass/auth/IdentityLinkingService;", "p2pTransportManager", "Lcom/example/teamcompass/p2p/P2PTransportManager;", "initializeAutoStartOverride", "Lkotlin/Function2;", "Lkotlin/coroutines/Continuation;", "", "", "Lkotlin/ExtensionFunctionType;", "savedStateHandle", "Landroidx/lifecycle/SavedStateHandle;", "<init>", "(Landroid/app/Application;Lcom/example/teamcompass/domain/TeamRepository;Lcom/example/teamcompass/domain/TrackingController;Lcom/example/teamcompass/ui/UserPrefs;Lcom/google/firebase/auth/FirebaseAuth;ZLcom/example/teamcompass/ui/ActionTraceIdProvider;Lcom/example/teamcompass/ui/StructuredLogger;Lkotlinx/coroutines/CoroutineExceptionHandler;Lcom/example/teamcompass/auth/IdentityLinkingService;Lcom/example/teamcompass/p2p/P2PTransportManager;Lkotlin/jvm/functions/Function2;Landroidx/lifecycle/SavedStateHandle;)V", "(Landroid/app/Application;Lcom/example/teamcompass/domain/TeamRepository;Lcom/example/teamcompass/domain/TrackingController;Lcom/example/teamcompass/ui/UserPrefs;Lcom/google/firebase/auth/FirebaseAuth;Lkotlinx/coroutines/CoroutineExceptionHandler;Lcom/example/teamcompass/p2p/P2PTransportManager;Landroidx/lifecycle/SavedStateHandle;)V", "Lkotlin/jvm/functions/Function2;", "application", "fusedPreview", "Lcom/google/android/gms/location/FusedLocationProviderClient;", "vibrator", "Landroid/os/Vibrator;", "kotlin.jvm.PlatformType", "Landroid/os/Vibrator;", "tone", "Landroid/media/ToneGenerator;", "tacticalFiltersEnabled", "locationReadinessCoordinator", "Lcom/example/teamcompass/ui/LocationReadinessCoordinator;", "targetFilterCoordinator", "Lcom/example/teamcompass/ui/TargetFilterCoordinator;", "authDelegate", "Lcom/example/teamcompass/ui/AuthDelegate;", "teamSessionDelegate", "Lcom/example/teamcompass/ui/TeamSessionDelegate;", "trackingCoordinator", "Lcom/example/teamcompass/ui/TrackingCoordinator;", "alertsCoordinator", "Lcom/example/teamcompass/ui/AlertsCoordinator;", "mapCoordinator", "Lcom/example/teamcompass/ui/MapCoordinator;", "joinRateLimiter", "Lcom/example/teamcompass/ui/JoinRateLimiter;", "backendHealthMonitor", "Lcom/example/teamcompass/ui/BackendHealthMonitor;", "backendHealthDelegate", "Lcom/example/teamcompass/ui/BackendHealthDelegate;", "teamSnapshotObserver", "Lcom/example/teamcompass/ui/TeamSnapshotObserver;", "memberPrefsSyncWorker", "Lcom/example/teamcompass/ui/MemberPrefsSyncWorker;", "eventNotificationManager", "Lcom/example/teamcompass/ui/EventNotificationManager;", "analytics", "Lcom/google/firebase/analytics/FirebaseAnalytics;", "autoBrightnessBinding", "Lcom/example/teamcompass/ui/AutoBrightnessBinding;", "teamObserverJob", "Lkotlinx/coroutines/Job;", "memberPrefsObserverJob", "memberPrefsSyncJob", "p2pObserverJob", "identityLinkingPromptTrackedUid", "", "deadReminderJob", "locationServiceMonitorJob", "targetFilterDirtyByUser", "lastBackendHealthAvailableSample", "restoredTeamCode", "restoredDefaultMode", "Lcom/example/teamcompass/core/TrackingMode;", "restoredPlayerMode", "Lcom/example/teamcompass/core/PlayerMode;", "restoredIsTracking", "restoredMySosUntilMs", "", "_ui", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/teamcompass/ui/UiState;", "ui", "Lkotlinx/coroutines/flow/StateFlow;", "getUi", "()Lkotlinx/coroutines/flow/StateFlow;", "_events", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/example/teamcompass/ui/UiEvent;", "events", "Lkotlinx/coroutines/flow/SharedFlow;", "getEvents", "()Lkotlinx/coroutines/flow/SharedFlow;", "headingSensorCoordinator", "Lcom/example/teamcompass/ui/HeadingSensorCoordinator;", "getHeadingSensorCoordinator", "()Lcom/example/teamcompass/ui/HeadingSensorCoordinator;", "headingSensorCoordinator$delegate", "Lkotlin/Lazy;", "bluetoothScanCoordinatorLazy", "Lkotlin/Lazy;", "Lcom/example/teamcompass/ui/BluetoothScanCoordinator;", "bluetoothScanCoordinator", "getBluetoothScanCoordinator", "()Lcom/example/teamcompass/ui/BluetoothScanCoordinator;", "tacticalActionsCoordinator", "Lcom/example/teamcompass/ui/TacticalActionsCoordinator;", "getTacticalActionsCoordinator", "()Lcom/example/teamcompass/ui/TacticalActionsCoordinator;", "tacticalActionsCoordinator$delegate", "newTraceId", "action", "logActionStart", "traceId", "teamCode", "uid", "logActionSuccess", "logActionFailure", "throwable", "", "message", "initializeAutoStart", "setUiForTest", "transform", "Lkotlin/Function1;", "setUiForTest$app_debug", "refreshBackendStaleFlagForTest", "nowMs", "refreshBackendStaleFlagForTest$app_debug", "emitError", "cause", "bindPrefs", "bindTrackingController", "ensureAuth", "onAuthReady", "setCallsign", "value", "setDefaultMode", "mode", "setTeamViewMode", "Lcom/example/teamcompass/domain/TeamViewMode;", "setGamePolicy", "intervalSec", "", "distanceM", "setSilentPolicy", "setThemeMode", "Lcom/example/teamcompass/ui/theme/ThemeMode;", "setLocationPermission", "granted", "refreshLocationReadiness", "bindSavedStateHandle", "refreshLocationPreview", "togglePlayerMode", "setPlayerMode", "setEnemyMarkEnabled", "enabled", "setTargetPreset", "preset", "Lcom/example/teamcompass/core/TargetFilterPreset;", "setNearRadius", "radiusM", "setShowDead", "showDead", "setShowStale", "showStale", "setFocusMode", "updateTargetFilterStateByUser", "Lcom/example/teamcompass/core/TargetFilterState;", "importTacticalMap", "uri", "Landroid/net/Uri;", "clearTacticalMap", "setMapEnabled", "setMapOpacity", "opacity", "", "saveMapChangesToSource", "newPoints", "", "Lcom/example/teamcompass/ui/KmlPoint;", "deletedPoints", "saveMapChangesAs", "saveMapChangesInternal", "destinationUri", "map", "Lcom/example/teamcompass/ui/TacticalMap;", "toggleSos", "triggerSos", "clearSos", "addPointHere", "label", "icon", "forTeam", "addPointAt", "lat", "", "lon", "updatePoint", "id", "isTeam", "deletePoint", "sendQuickCommand", "type", "Lcom/example/teamcompass/ui/QuickCommandType;", "addEnemyPing", "assignTeamMemberRole", "targetUid", "patch", "Lcom/example/teamcompass/domain/TeamRolePatch;", "assignTeamMemberRolesBulk", "targetUids", "createTeam", "joinTeam", "codeRaw", "alsoCreateMember", "onTeamJoined", "code", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "evaluateIdentityLinkingEligibility", "markCompassHelpSeen", "markOnboardingSeen", "setControlLayoutEdit", "setControlPosition", "Lcom/example/teamcompass/ui/CompassControlId;", "position", "Lcom/example/teamcompass/ui/ControlPosition;", "resetControlPositions", "applyControlLayoutPreset", "Lcom/example/teamcompass/ui/ControlLayoutPreset;", "leaveTeam", "startListening", "handleStartListeningTerminalFailure", "failure", "Lcom/example/teamcompass/domain/TeamActionFailure;", "clearTeamSessionStateForTerminalFailure", "collectTeamSnapshotsWithReconnect", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "stopListening", "startP2PInboundObservation", "localUid", "handleP2PInbound", "inbound", "Lcom/example/teamcompass/p2p/P2PInboundMessage;", "startBackendHealthMonitor", "startBackendStaleMonitor", "scheduleBackendStaleRefresh", "refreshBackendStaleFlag", "computeBackendStale", "lastSnapshotAtMs", "startMemberPrefsSync", "processEnemyPingAlerts", "enemyPings", "Lcom/example/teamcompass/ui/EnemyPing;", "processSosAlerts", "vibrateAndBeep", "strong", "startDeadReminder", "stopDeadReminder", "startTracking", "persistMode", "restartTracking", "stopTracking", "dismissError", "computeTargets", "Lcom/example/teamcompass/core/CompassTarget;", "refreshTargetsFromState", "startHeading", "stopHeading", "startLocationServiceMonitor", "normalizeTeamCode", "raw", "tr", "resId", "args", "", "(I[Ljava/lang/Object;)Ljava/lang/String;", "handleActionFailure", "defaultMessage", "logPerfMetricsSnapshot", "onCleared", "bindAutoBrightnessWindow", "window", "Landroid/view/Window;", "setAutoBrightnessEnabled", "setScreenBrightness", "brightness", "setHasStartedOnce", "autoStartTrackingIfNeeded", "hasBluetoothPermission", "startBluetoothScan", "cancelBluetoothScan", "Companion", "RestorableVmState", "app_debug"})
@HiltViewModel
@StabilityInferred(parameters=0)
@SourceDebugExtension(value={"SMAP\nTeamCompassViewModel.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TeamCompassViewModel.kt\ncom/example/teamcompass/ui/TeamCompassViewModel\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 3 CoroutineExceptionHandler.kt\nkotlinx/coroutines/CoroutineExceptionHandlerKt\n+ 4 StateFlow.kt\nkotlinx/coroutines/flow/StateFlowKt\n+ 5 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 6 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt\n+ 7 SafeCollector.common.kt\nkotlinx/coroutines/flow/internal/SafeCollector_commonKt\n+ 8 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,1920:1\n1#2:1921\n46#3,4:1922\n230#4,5:1926\n230#4,5:1931\n230#4,5:1936\n230#4,5:1941\n230#4,5:1946\n230#4,5:1951\n230#4,5:1956\n230#4,5:1961\n230#4,5:1966\n230#4,5:1971\n230#4,5:1976\n230#4,5:1981\n230#4,5:1986\n230#4,5:1991\n230#4,5:1996\n230#4,5:2001\n230#4,5:2006\n230#4,5:2011\n230#4,5:2016\n230#4,5:2021\n230#4,5:2026\n230#4,5:2031\n230#4,5:2036\n230#4,5:2041\n230#4,5:2046\n230#4,5:2051\n230#4,5:2056\n230#4,5:2061\n230#4,5:2066\n230#4,5:2071\n230#4,5:2076\n230#4,5:2081\n230#4,5:2086\n230#4,5:2091\n230#4,5:2096\n230#4,5:2101\n230#4,5:2106\n230#4,5:2111\n230#4,5:2116\n230#4,5:2131\n230#4,5:2136\n230#4,5:2141\n230#4,5:2146\n230#4,5:2151\n230#4,5:2156\n230#4,5:2161\n230#4,5:2166\n230#4,5:2171\n230#4,5:2176\n230#4,5:2181\n230#4,5:2186\n230#4,5:2191\n230#4,5:2196\n230#4,5:2201\n49#5:2121\n51#5:2125\n46#6:2122\n51#6:2124\n105#7:2123\n827#8:2126\n855#8,2:2127\n1863#8,2:2129\n*S KotlinDebug\n*F\n+ 1 TeamCompassViewModel.kt\ncom/example/teamcompass/ui/TeamCompassViewModel\n*L\n77#1:1922,4\n329#1:1926,5\n336#1:1931,5\n346#1:1936,5\n522#1:1941,5\n528#1:1946,5\n534#1:1951,5\n542#1:1956,5\n552#1:1961,5\n562#1:1966,5\n570#1:1971,5\n575#1:1976,5\n576#1:1981,5\n589#1:1986,5\n630#1:1991,5\n664#1:1996,5\n675#1:2001,5\n704#1:2006,5\n715#1:2011,5\n733#1:2016,5\n737#1:2021,5\n741#1:2026,5\n794#1:2031,5\n838#1:2036,5\n843#1:2041,5\n979#1:2046,5\n1021#1:2051,5\n1061#1:2056,5\n1108#1:2061,5\n1113#1:2066,5\n1118#1:2071,5\n1124#1:2076,5\n1130#1:2081,5\n1136#1:2086,5\n1149#1:2091,5\n1275#1:2096,5\n1389#1:2101,5\n1438#1:2106,5\n1518#1:2111,5\n1551#1:2116,5\n1673#1:2131,5\n1676#1:2136,5\n1692#1:2141,5\n1730#1:2146,5\n1734#1:2151,5\n1741#1:2156,5\n1757#1:2161,5\n1862#1:2166,5\n219#1:2171,5\n229#1:2176,5\n241#1:2181,5\n509#1:2186,5\n637#1:2191,5\n1582#1:2196,5\n1780#1:2201,5\n1578#1:2121\n1578#1:2125\n1578#1:2122\n1578#1:2124\n1578#1:2123\n1603#1:2126\n1603#1:2127,2\n1622#1:2129,2\n*E\n"})
public final class TeamCompassViewModel
extends AndroidViewModel {
    @NotNull
    public static final Companion Companion = new Companion(null);
    @NotNull
    private final TeamRepository teamRepository;
    @NotNull
    private final TrackingController trackingController;
    @NotNull
    private final UserPrefs prefs;
    @NotNull
    private final FirebaseAuth auth;
    private final boolean autoStart;
    @NotNull
    private final ActionTraceIdProvider actionTraceIdProvider;
    @NotNull
    private final StructuredLogger structuredLogger;
    @NotNull
    private final CoroutineExceptionHandler coroutineExceptionHandler;
    @NotNull
    private final IdentityLinkingService identityLinkingService;
    @Nullable
    private final P2PTransportManager p2pTransportManager;
    @Nullable
    private final Function2<TeamCompassViewModel, Continuation<? super Unit>, Object> initializeAutoStartOverride;
    @Nullable
    private final SavedStateHandle savedStateHandle;
    @NotNull
    private final Application application;
    @NotNull
    private final FusedLocationProviderClient fusedPreview;
    private final Vibrator vibrator;
    @NotNull
    private final ToneGenerator tone;
    private final boolean tacticalFiltersEnabled;
    @NotNull
    private final LocationReadinessCoordinator locationReadinessCoordinator;
    @NotNull
    private final TargetFilterCoordinator targetFilterCoordinator;
    @NotNull
    private final AuthDelegate authDelegate;
    @NotNull
    private final TeamSessionDelegate teamSessionDelegate;
    @NotNull
    private final TrackingCoordinator trackingCoordinator;
    @NotNull
    private final AlertsCoordinator alertsCoordinator;
    @NotNull
    private final MapCoordinator mapCoordinator;
    @NotNull
    private final JoinRateLimiter joinRateLimiter;
    @NotNull
    private final BackendHealthMonitor backendHealthMonitor;
    @NotNull
    private final BackendHealthDelegate backendHealthDelegate;
    @NotNull
    private final TeamSnapshotObserver teamSnapshotObserver;
    @NotNull
    private final MemberPrefsSyncWorker memberPrefsSyncWorker;
    @NotNull
    private final EventNotificationManager eventNotificationManager;
    @NotNull
    private final FirebaseAnalytics analytics;
    @NotNull
    private final AutoBrightnessBinding autoBrightnessBinding;
    @Nullable
    private Job teamObserverJob;
    @Nullable
    private Job memberPrefsObserverJob;
    @Nullable
    private Job memberPrefsSyncJob;
    @Nullable
    private Job p2pObserverJob;
    @Nullable
    private String identityLinkingPromptTrackedUid;
    @Nullable
    private Job deadReminderJob;
    @Nullable
    private Job locationServiceMonitorJob;
    private boolean targetFilterDirtyByUser;
    private boolean lastBackendHealthAvailableSample;
    @Nullable
    private final String restoredTeamCode;
    @NotNull
    private final TrackingMode restoredDefaultMode;
    @NotNull
    private final PlayerMode restoredPlayerMode;
    private final boolean restoredIsTracking;
    private final long restoredMySosUntilMs;
    @NotNull
    private final MutableStateFlow<UiState> _ui;
    @NotNull
    private final StateFlow<UiState> ui;
    @NotNull
    private final MutableSharedFlow<UiEvent> _events;
    @NotNull
    private final SharedFlow<UiEvent> events;
    @NotNull
    private final Lazy headingSensorCoordinator$delegate;
    @NotNull
    private final Lazy<BluetoothScanCoordinator> bluetoothScanCoordinatorLazy;
    @NotNull
    private final Lazy tacticalActionsCoordinator$delegate;
    public static final int $stable = 8;
    @NotNull
    private static final String TAG = "TeamCompassVM";
    @NotNull
    private static final String INIT_FAILURE_MESSAGE = "Initialization failed";
    private static final long OP_TIMEOUT_MS = 15000L;
    private static final long SOS_DURATION_MS = 60000L;
    private static final long TEAM_RECONNECT_INITIAL_DELAY_MS = 1500L;
    private static final long TEAM_RECONNECT_MAX_DELAY_MS = 20000L;
    public static final long STALE_WARNING_MS = 30000L;
    @NotNull
    private static final String STATE_TEAM_CODE = "state_team_code";
    @NotNull
    private static final String STATE_DEFAULT_MODE = "state_default_mode";
    @NotNull
    private static final String STATE_PLAYER_MODE = "state_player_mode";
    @NotNull
    private static final String STATE_IS_TRACKING = "state_is_tracking";
    @NotNull
    private static final String STATE_MY_SOS_UNTIL_MS = "state_my_sos_until_ms";

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public TeamCompassViewModel(@NotNull Application app, @NotNull TeamRepository teamRepository, @NotNull TrackingController trackingController, @NotNull UserPrefs prefs, @NotNull FirebaseAuth auth, boolean autoStart, @NotNull ActionTraceIdProvider actionTraceIdProvider, @NotNull StructuredLogger structuredLogger, @NotNull CoroutineExceptionHandler coroutineExceptionHandler, @NotNull IdentityLinkingService identityLinkingService, @Nullable P2PTransportManager p2pTransportManager, @Nullable Function2<? super TeamCompassViewModel, ? super Continuation<? super Unit>, ? extends Object> initializeAutoStartOverride, @Nullable SavedStateHandle savedStateHandle) {
        Intrinsics.checkNotNullParameter((Object)app, (String)"app");
        Intrinsics.checkNotNullParameter((Object)teamRepository, (String)"teamRepository");
        Intrinsics.checkNotNullParameter((Object)trackingController, (String)"trackingController");
        Intrinsics.checkNotNullParameter((Object)prefs, (String)"prefs");
        Intrinsics.checkNotNullParameter((Object)auth, (String)"auth");
        Intrinsics.checkNotNullParameter((Object)actionTraceIdProvider, (String)"actionTraceIdProvider");
        Intrinsics.checkNotNullParameter((Object)structuredLogger, (String)"structuredLogger");
        Intrinsics.checkNotNullParameter((Object)coroutineExceptionHandler, (String)"coroutineExceptionHandler");
        Intrinsics.checkNotNullParameter((Object)identityLinkingService, (String)"identityLinkingService");
        super(app);
        this.teamRepository = teamRepository;
        this.trackingController = trackingController;
        this.prefs = prefs;
        this.auth = auth;
        this.autoStart = autoStart;
        this.actionTraceIdProvider = actionTraceIdProvider;
        this.structuredLogger = structuredLogger;
        this.coroutineExceptionHandler = coroutineExceptionHandler;
        this.identityLinkingService = identityLinkingService;
        this.p2pTransportManager = p2pTransportManager;
        this.initializeAutoStartOverride = initializeAutoStartOverride;
        this.savedStateHandle = savedStateHandle;
        this.application = this.getApplication();
        v0 = LocationServices.getFusedLocationProviderClient((Context)((Context)this.application));
        Intrinsics.checkNotNullExpressionValue((Object)v0, (String)"getFusedLocationProviderClient(...)");
        this.fusedPreview = v0;
        this.vibrator = (Vibrator)this.application.getSystemService(Vibrator.class);
        this.tone = new ToneGenerator(4, 100);
        this.tacticalFiltersEnabled = true;
        this.locationReadinessCoordinator = new LocationReadinessCoordinator(this.application);
        this.targetFilterCoordinator = new TargetFilterCoordinator(this.tacticalFiltersEnabled, null, 2, null);
        this.authDelegate = new AuthDelegate(new FirebaseAuthGateway(this.auth));
        this.teamSessionDelegate = new TeamSessionDelegate(new SessionCoordinator(this.teamRepository, 15000L, null, 4, null), this.p2pTransportManager);
        this.trackingCoordinator = new TrackingCoordinator(this.trackingController, null, 2, null);
        this.alertsCoordinator = new AlertsCoordinator(0L, 0.0, 3, null);
        this.mapCoordinator = new MapCoordinator(null, null, 3, null);
        this.joinRateLimiter = new JoinRateLimiter(null, 0L, 0, 0, 15, null);
        this.backendHealthMonitor = new BackendHealthMonitor(this.teamRepository);
        this.backendHealthDelegate = new BackendHealthDelegate(this.backendHealthMonitor, 30000L, null, null, 12, null);
        this.teamSnapshotObserver = new TeamSnapshotObserver(this.teamRepository, 1500L, 20000L, null, 8, null);
        this.memberPrefsSyncWorker = new MemberPrefsSyncWorker(this.teamRepository, this.tacticalFiltersEnabled);
        this.eventNotificationManager = new EventNotificationManager(this.application);
        v1 = FirebaseAnalytics.getInstance((Context)((Context)this.application));
        Intrinsics.checkNotNullExpressionValue((Object)v1, (String)"getInstance(...)");
        this.analytics = v1;
        this.autoBrightnessBinding = new AutoBrightnessBinding(this.application, (Function1<? super Throwable, Unit>)(Function1)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, autoBrightnessBinding$lambda$1(java.lang.Throwable ), (Ljava/lang/Throwable;)Lkotlin/Unit;)());
        this.lastBackendHealthAvailableSample = true;
        v2 = this;
        var14_14 = this.savedStateHandle;
        if (var14_14 == null || (var15_15 = (String)var14_14.get("state_team_code")) == null) ** GOTO lbl-1000
        var16_16 = StringsKt.trim((CharSequence)((CharSequence)var15_15)).toString();
        if (var16_16 != null) {
            var17_17 = var16_16;
            var18_18 = var17_17;
            var22_19 = v2;
            $i$a$-takeIf-TeamCompassViewModel$restoredTeamCode$1 = false;
            var23_23 = ((CharSequence)it).length() > 0;
            v2 = var22_19;
            v3 = var23_23 ? var17_17 : null;
        } else lbl-1000:
        // 2 sources

        {
            v3 = null;
        }
        v2.restoredTeamCode = v3;
        v4 = this;
        var14_14 = this.savedStateHandle;
        if (var14_14 == null || (var15_15 = (String)var14_14.get("state_default_mode")) == null) ** GOTO lbl-1000
        var17_17 = var15_15;
        var18_18 = TeamCompassViewModel.Companion;
        $i$a$-takeIf-TeamCompassViewModel$restoredTeamCode$1 = var17_17;
        var22_19 = v4;
        $i$f$restoredDefaultMode$stub_for_inlining = false;
        $i$a$-let-TeamCompassViewModel$restoredDefaultMode$1 = false;
        v4 = var22_19;
        var16_16 = com.example.teamcompass.ui.TeamCompassViewModel$Companion.access$parseTrackingModeOrNull((Companion)var18_18, (String)p0);
        if (var16_16 != null) {
            v5 /* !! */  = var16_16;
        } else lbl-1000:
        // 2 sources

        {
            v5 /* !! */  = TrackingMode.GAME;
        }
        v4.restoredDefaultMode = v5 /* !! */ ;
        v6 = this;
        var14_14 = this.savedStateHandle;
        if (var14_14 == null || (var15_15 = (String)var14_14.get("state_player_mode")) == null) ** GOTO lbl-1000
        var17_17 = var15_15;
        var18_18 = TeamCompassViewModel.Companion;
        p0 = var17_17;
        var22_19 = v6;
        $i$f$restoredPlayerMode$stub_for_inlining$3 = false;
        $i$a$-let-TeamCompassViewModel$restoredPlayerMode$1 = false;
        v6 = var22_19;
        var16_16 = com.example.teamcompass.ui.TeamCompassViewModel$Companion.access$parsePlayerModeOrNull((Companion)var18_18, p0);
        if (var16_16 != null) {
            v7 /* !! */  = var16_16;
        } else lbl-1000:
        // 2 sources

        {
            v7 /* !! */  = PlayerMode.GAME;
        }
        v6.restoredPlayerMode = v7 /* !! */ ;
        var14_14 = this.savedStateHandle;
        this.restoredIsTracking = var14_14 != null && (var15_15 = (Boolean)var14_14.get("state_is_tracking")) != null ? var15_15.booleanValue() : false;
        var14_14 = this.savedStateHandle;
        this.restoredMySosUntilMs = var14_14 != null && (var15_15 = (Long)var14_14.get("state_my_sos_until_ms")) != null ? var15_15.longValue() : 0L;
        this._ui = StateFlowKt.MutableStateFlow((Object)new UiState(null, new TrackingUiState(this.restoredIsTracking, this.locationReadinessCoordinator.hasLocationPermission(), this.locationReadinessCoordinator.isLocationServiceEnabled(), null, null, this.restoredDefaultMode, false, false, null, 472, null), new TeamUiState(null, this.restoredTeamCode, null, null, this.restoredPlayerMode, this.restoredMySosUntilMs, null, null, false, 461, null), null, null, null, null, null, 249, null));
        this.ui = FlowKt.asStateFlow(this._ui);
        this._events = SharedFlowKt.MutableSharedFlow$default((int)0, (int)32, null, (int)5, null);
        this.events = FlowKt.asSharedFlow(this._events);
        this.headingSensorCoordinator$delegate = LazyKt.lazy((Function0)(Function0)LambdaMetafactory.metafactory(null, null, null, ()Ljava/lang/Object;, headingSensorCoordinator_delegate$lambda$6(com.example.teamcompass.ui.TeamCompassViewModel ), ()Lcom/example/teamcompass/ui/HeadingSensorCoordinator;)((TeamCompassViewModel)this));
        this.bluetoothScanCoordinatorLazy = LazyKt.lazy((Function0)(Function0)LambdaMetafactory.metafactory(null, null, null, ()Ljava/lang/Object;, bluetoothScanCoordinatorLazy$lambda$10(com.example.teamcompass.ui.TeamCompassViewModel ), ()Lcom/example/teamcompass/ui/BluetoothScanCoordinator;)((TeamCompassViewModel)this));
        this.tacticalActionsCoordinator$delegate = LazyKt.lazy((Function0)(Function0)LambdaMetafactory.metafactory(null, null, null, ()Ljava/lang/Object;, tacticalActionsCoordinator_delegate$lambda$14(com.example.teamcompass.ui.TeamCompassViewModel ), ()Lcom/example/teamcompass/ui/TacticalActionsCoordinator;)((TeamCompassViewModel)this));
        this.bindSavedStateHandle();
        if (this.autoStart) {
            BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
                Object L$0;
                int label;
                final /* synthetic */ TeamCompassViewModel this$0;
                {
                    this.this$0 = $receiver;
                    super(2, $completion);
                }

                /*
                 * Exception decompiling
                 */
                public final Object invokeSuspend(Object var1_1) {
                    /*
                     * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
                     * 
                     * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [4[CASE], 2[SWITCH]], but top level block is 1[TRYBLOCK]
                     *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
                     *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
                     *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
                     *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
                     *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                     *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                     *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                     *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
                     *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
                     *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
                     *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
                     *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
                     *     at org.benf.cfr.reader.Driver.doClass(Driver.java:84)
                     *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:78)
                     *     at org.benf.cfr.reader.Main.main(Main.java:54)
                     */
                    throw new IllegalStateException("Decompilation failed");
                }

                public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                    return (Continuation)new /* invalid duplicate definition of identical inner class */;
                }

                public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                    return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                }
            }), (int)2, null);
        }
    }

    public /* synthetic */ TeamCompassViewModel(Application application, TeamRepository teamRepository, TrackingController trackingController, UserPrefs userPrefs, FirebaseAuth firebaseAuth, boolean bl, ActionTraceIdProvider actionTraceIdProvider, StructuredLogger structuredLogger, CoroutineExceptionHandler coroutineExceptionHandler, IdentityLinkingService identityLinkingService, P2PTransportManager p2PTransportManager, Function2 function2, SavedStateHandle savedStateHandle, int n, DefaultConstructorMarker defaultConstructorMarker) {
        if ((n & 0x10) != 0) {
            firebaseAuth = FirebaseAuth.getInstance();
        }
        if ((n & 0x20) != 0) {
            bl = true;
        }
        if ((n & 0x40) != 0) {
            actionTraceIdProvider = new UuidActionTraceIdProvider();
        }
        if ((n & 0x80) != 0) {
            structuredLogger = new CrashlyticsStructuredLogger(null, null, 3, null);
        }
        if ((n & 0x100) != 0) {
            boolean $i$f$CoroutineExceptionHandler = false;
            CoroutineExceptionHandler.Key key = CoroutineExceptionHandler.Key;
            coroutineExceptionHandler = new CoroutineExceptionHandler(key){

                public void handleException(CoroutineContext context, Throwable exception) {
                    Throwable err = exception;
                    boolean bl = false;
                    Log.e((String)"TeamCompassVM", (String)"Unhandled coroutine exception", (Throwable)err);
                }
            };
        }
        if ((n & 0x200) != 0) {
            identityLinkingService = NoOpIdentityLinkingService.INSTANCE;
        }
        if ((n & 0x400) != 0) {
            p2PTransportManager = null;
        }
        if ((n & 0x800) != 0) {
            function2 = null;
        }
        if ((n & 0x1000) != 0) {
            savedStateHandle = null;
        }
        this(application, teamRepository, trackingController, userPrefs, firebaseAuth, bl, actionTraceIdProvider, structuredLogger, coroutineExceptionHandler, identityLinkingService, p2PTransportManager, (Function2<? super TeamCompassViewModel, ? super Continuation<? super Unit>, ? extends Object>)function2, savedStateHandle);
    }

    @Inject
    public TeamCompassViewModel(@NotNull Application app, @NotNull TeamRepository teamRepository, @NotNull TrackingController trackingController, @NotNull UserPrefs prefs, @NotNull FirebaseAuth auth, @NotNull CoroutineExceptionHandler coroutineExceptionHandler, @NotNull P2PTransportManager p2pTransportManager, @NotNull SavedStateHandle savedStateHandle) {
        Intrinsics.checkNotNullParameter((Object)app, (String)"app");
        Intrinsics.checkNotNullParameter((Object)teamRepository, (String)"teamRepository");
        Intrinsics.checkNotNullParameter((Object)trackingController, (String)"trackingController");
        Intrinsics.checkNotNullParameter((Object)prefs, (String)"prefs");
        Intrinsics.checkNotNullParameter((Object)auth, (String)"auth");
        Intrinsics.checkNotNullParameter((Object)coroutineExceptionHandler, (String)"coroutineExceptionHandler");
        Intrinsics.checkNotNullParameter((Object)p2pTransportManager, (String)"p2pTransportManager");
        Intrinsics.checkNotNullParameter((Object)savedStateHandle, (String)"savedStateHandle");
        this(app, teamRepository, trackingController, prefs, auth, true, new UuidActionTraceIdProvider(), new CrashlyticsStructuredLogger(null, null, 3, null), coroutineExceptionHandler, new FirebaseIdentityLinkingService(auth), p2pTransportManager, null, savedStateHandle);
    }

    @NotNull
    public final StateFlow<UiState> getUi() {
        return this.ui;
    }

    @NotNull
    public final SharedFlow<UiEvent> getEvents() {
        return this.events;
    }

    private final HeadingSensorCoordinator getHeadingSensorCoordinator() {
        Lazy lazy = this.headingSensorCoordinator$delegate;
        return (HeadingSensorCoordinator)lazy.getValue();
    }

    private final BluetoothScanCoordinator getBluetoothScanCoordinator() {
        return (BluetoothScanCoordinator)this.bluetoothScanCoordinatorLazy.getValue();
    }

    private final TacticalActionsCoordinator getTacticalActionsCoordinator() {
        Lazy lazy = this.tacticalActionsCoordinator$delegate;
        return (TacticalActionsCoordinator)lazy.getValue();
    }

    private final String newTraceId(String action) {
        return this.actionTraceIdProvider.nextTraceId(action);
    }

    private final void logActionStart(String action, String traceId, String teamCode, String uid) {
        this.structuredLogger.logStart(action, traceId, teamCode, uid, ((UiState)this._ui.getValue()).getTelemetry().getBackendAvailable());
    }

    static /* synthetic */ void logActionStart$default(TeamCompassViewModel teamCompassViewModel, String string2, String string3, String string4, String string5, int n, Object object) {
        if ((n & 4) != 0) {
            string4 = ((UiState)teamCompassViewModel._ui.getValue()).getTeamCode();
        }
        if ((n & 8) != 0) {
            string5 = ((UiState)teamCompassViewModel._ui.getValue()).getUid();
        }
        teamCompassViewModel.logActionStart(string2, string3, string4, string5);
    }

    private final void logActionSuccess(String action, String traceId, String teamCode, String uid) {
        this.structuredLogger.logSuccess(action, traceId, teamCode, uid, ((UiState)this._ui.getValue()).getTelemetry().getBackendAvailable());
    }

    static /* synthetic */ void logActionSuccess$default(TeamCompassViewModel teamCompassViewModel, String string2, String string3, String string4, String string5, int n, Object object) {
        if ((n & 4) != 0) {
            string4 = ((UiState)teamCompassViewModel._ui.getValue()).getTeamCode();
        }
        if ((n & 8) != 0) {
            string5 = ((UiState)teamCompassViewModel._ui.getValue()).getUid();
        }
        teamCompassViewModel.logActionSuccess(string2, string3, string4, string5);
    }

    private final void logActionFailure(String action, String traceId, Throwable throwable, String message, String teamCode, String uid) {
        this.structuredLogger.logFailure(action, traceId, teamCode, uid, ((UiState)this._ui.getValue()).getTelemetry().getBackendAvailable(), throwable, message);
    }

    static /* synthetic */ void logActionFailure$default(TeamCompassViewModel teamCompassViewModel, String string2, String string3, Throwable throwable, String string4, String string5, String string6, int n, Object object) {
        if ((n & 4) != 0) {
            throwable = null;
        }
        if ((n & 8) != 0) {
            string4 = null;
        }
        if ((n & 0x10) != 0) {
            string5 = ((UiState)teamCompassViewModel._ui.getValue()).getTeamCode();
        }
        if ((n & 0x20) != 0) {
            string6 = ((UiState)teamCompassViewModel._ui.getValue()).getUid();
        }
        teamCompassViewModel.logActionFailure(string2, string3, throwable, string4, string5, string6);
    }

    private final void initializeAutoStart() {
        UiState state;
        UiState nextValue$iv;
        Object prevValue$iv;
        this.bindPrefs();
        this.bindTrackingController();
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            state = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = this.locationReadinessCoordinator.applyServiceState(state))));
        this.startLocationServiceMonitor();
        this.ensureAuth();
    }

    @VisibleForTesting
    public final void setUiForTest$app_debug(@NotNull Function1<? super UiState, UiState> transform) {
        Object nextValue$iv;
        Object prevValue$iv;
        Intrinsics.checkNotNullParameter(transform, (String)"transform");
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        while (!$this$update$iv.compareAndSet(prevValue$iv = $this$update$iv.getValue(), nextValue$iv = transform.invoke(prevValue$iv))) {
        }
    }

    @VisibleForTesting
    public final void refreshBackendStaleFlagForTest$app_debug(long nowMs) {
        this.refreshBackendStaleFlag(nowMs);
    }

    public final void emitError(@NotNull String message, @Nullable Throwable cause) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        Intrinsics.checkNotNullParameter((Object)message, (String)"message");
        int n = cause != null ? Log.w((String)TAG, (String)message, (Throwable)cause) : Log.w((String)TAG, (String)message);
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, null, null, message, 127, null))));
        this._events.tryEmit((Object)new UiEvent.Error(message));
    }

    public static /* synthetic */ void emitError$default(TeamCompassViewModel teamCompassViewModel, String string2, Throwable throwable, int n, Object object) {
        if ((n & 2) != 0) {
            throwable = null;
        }
        teamCompassViewModel.emitError(string2, throwable);
    }

    private final void bindPrefs() {
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getPrefs$p(this.this$0).getCallsignFlow(), (Function2)((Function2)new Function2<String, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ Object L$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        String callsign = (String)this.L$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, TeamUiState.copy$default(it.getTeam(), callsign, null, null, null, null, 0L, null, null, false, 510, null), null, null, null, null, null, 251, null))));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.L$0 = value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(String p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getPrefs$p(this.this$0).getTeamCodeFlow(), (Function2)((Function2)new Function2<String, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ Object L$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        String storedCode = (String)this.L$0;
                                        String normalized = TeamCompassViewModel.access$normalizeTeamCode(this.this$0, storedCode);
                                        String current = ((UiState)TeamCompassViewModel.access$get_ui$p(this.this$0).getValue()).getTeamCode();
                                        if (Intrinsics.areEqual((Object)normalized, (Object)current)) {
                                            return Unit.INSTANCE;
                                        }
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, TeamUiState.copy$default(it.getTeam(), null, normalized, null, null, null, 0L, null, null, false, 509, null), null, null, null, null, null, 251, null))));
                                        if (((UiState)TeamCompassViewModel.access$get_ui$p(this.this$0).getValue()).isAuthReady() && normalized != null) {
                                            TeamCompassViewModel.access$startListening(this.this$0, normalized);
                                        } else if (normalized == null) {
                                            TeamCompassViewModel.access$stopListening(this.this$0);
                                        }
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.L$0 = value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(String p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getPrefs$p(this.this$0).getDefaultModeFlow(), (Function2)((Function2)new Function2<TrackingMode, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ Object L$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        TrackingMode mode = (TrackingMode)this.L$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, TrackingUiState.copy$default(it.getTracking(), false, false, false, null, null, mode, false, false, null, 479, null), null, null, null, null, null, null, 253, null))));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.L$0 = value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(TrackingMode p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getPrefs$p(this.this$0).getGameIntervalSecFlow(), (Function2)((Function2)new Function2<Integer, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ int I$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        int sec = this.I$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), sec, 0, 0, 0, false, 0.0f, null, false, null, false, false, 2046, null), null, null, 223, null))));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.I$0 = ((Number)value).intValue();
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(int p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getPrefs$p(this.this$0).getGameDistanceMFlow(), (Function2)((Function2)new Function2<Integer, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ int I$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        int distance = this.I$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, distance, 0, 0, false, 0.0f, null, false, null, false, false, 2045, null), null, null, 223, null))));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.I$0 = ((Number)value).intValue();
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(int p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getPrefs$p(this.this$0).getSilentIntervalSecFlow(), (Function2)((Function2)new Function2<Integer, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ int I$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        int sec = this.I$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, sec, 0, false, 0.0f, null, false, null, false, false, 2043, null), null, null, 223, null))));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.I$0 = ((Number)value).intValue();
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(int p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getPrefs$p(this.this$0).getSilentDistanceMFlow(), (Function2)((Function2)new Function2<Integer, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ int I$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        int distance = this.I$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, distance, false, 0.0f, null, false, null, false, false, 2039, null), null, null, 223, null))));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.I$0 = ((Number)value).intValue();
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(int p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getPrefs$p(this.this$0).getShowCompassHelpOnceFlow(), (Function2)((Function2)new Function2<Boolean, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ boolean Z$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        boolean value = this.Z$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0f, null, false, null, value, false, 1535, null), null, null, 223, null))));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.Z$0 = (Boolean)value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(boolean p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getPrefs$p(this.this$0).getShowOnboardingOnceFlow(), (Function2)((Function2)new Function2<Boolean, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ boolean Z$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        boolean value = this.Z$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0f, null, false, null, false, value, 1023, null), null, null, 223, null))));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.Z$0 = (Boolean)value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(boolean p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getPrefs$p(this.this$0).getControlLayoutEditFlow(), (Function2)((Function2)new Function2<Boolean, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ boolean Z$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        boolean enabled = this.Z$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0f, null, enabled, null, false, false, 1919, null), null, null, 223, null))));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.Z$0 = (Boolean)value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(boolean p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getPrefs$p(this.this$0).getControlPositionsFlow(), (Function2)((Function2)new Function2<Map<CompassControlId, ? extends ControlPosition>, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ Object L$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        Map positions = (Map)this.L$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0f, null, false, positions, false, false, 1791, null), null, null, 223, null))));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.L$0 = value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(Map<CompassControlId, ControlPosition> p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getPrefs$p(this.this$0).getAutoBrightnessEnabledFlow(), (Function2)((Function2)new Function2<Boolean, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ boolean Z$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        boolean enabled = this.Z$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, enabled, 0.0f, null, false, null, false, false, 2031, null), null, null, 223, null))));
                                        TeamCompassViewModel.access$getAutoBrightnessBinding$p(this.this$0).setEnabled(enabled);
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.Z$0 = (Boolean)value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(boolean p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getPrefs$p(this.this$0).getScreenBrightnessFlow(), (Function2)((Function2)new Function2<Float, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ float F$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        float brightness = this.F$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, brightness, null, false, null, false, false, 2015, null), null, null, 223, null))));
                                        TeamCompassViewModel.access$getAutoBrightnessBinding$p(this.this$0).setBrightness(brightness);
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.F$0 = ((Number)value).floatValue();
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(float p1, Continuation<? super Unit> p2) {
                                return (this.create(Float.valueOf(p1), p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getPrefs$p(this.this$0).getHasStartedOnceFlow(), (Function2)((Function2)new Function2<Boolean, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ boolean Z$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        boolean hasStarted = this.Z$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, TrackingUiState.copy$default(it.getTracking(), false, false, false, null, null, null, hasStarted, false, null, 447, null), null, null, null, null, null, null, 253, null))));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.Z$0 = (Boolean)value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(boolean p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getPrefs$p(this.this$0).getThemeModeFlow(), (Function2)((Function2)new Function2<ThemeMode, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ Object L$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        ThemeMode mode = (ThemeMode)((Object)this.L$0);
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0f, mode, false, null, false, false, 1983, null), null, null, 223, null))));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.L$0 = value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(ThemeMode p1, Continuation<? super Unit> p2) {
                                return (this.create((Object)((Object)p1), p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    private final void bindTrackingController() {
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest((Flow)((Flow)TeamCompassViewModel.access$getTrackingController$p(this.this$0).isTracking()), (Function2)((Function2)new Function2<Boolean, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ boolean Z$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        boolean tracking = this.Z$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, TrackingUiState.copy$default(it.getTracking(), tracking, false, false, null, null, null, false, false, null, 510, null), null, null, null, null, null, null, 253, null))));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.Z$0 = (Boolean)value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(boolean p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest((Flow)((Flow)TeamCompassViewModel.access$getTrackingController$p(this.this$0).getLocation()), (Function2)((Function2)new Function2<LocationPoint, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ Object L$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        TelemetryState tel;
                                        UiState state;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        LocationPoint location = (LocationPoint)this.L$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            state = (UiState)prevValue$iv;
                                            boolean bl = false;
                                            TelemetryState telemetryState = tel = location == null ? state.getTracking().getTelemetry() : TelemetryState.copy$default(state.getTracking().getTelemetry(), 0, 0, 0, location.getTimestampMs(), null, false, 0L, 0L, false, 0, 0, 2039, null);
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(state, null, TrackingUiState.copy$default(state.getTracking(), false, false, false, location, null, null, false, false, tel, 247, null), null, null, null, null, null, null, 253, null))));
                                        TeamCompassViewModel.refreshTargetsFromState$default(this.this$0, 0L, 1, null);
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.L$0 = value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(LocationPoint p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest((Flow)((Flow)TeamCompassViewModel.access$getTrackingController$p(this.this$0).isAnchored()), (Function2)((Function2)new Function2<Boolean, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ boolean Z$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        UiState it;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        boolean anchored = this.Z$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            it = (UiState)prevValue$iv;
                                            boolean bl = false;
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, TrackingUiState.copy$default(it.getTracking(), false, false, false, null, null, null, false, anchored, null, 383, null), null, null, null, null, null, null, 253, null))));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.Z$0 = (Boolean)value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(boolean p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest((Flow)((Flow)TeamCompassViewModel.access$getTrackingController$p(this.this$0).getTelemetry()), (Function2)((Function2)new Function2<TrackingTelemetry, Continuation<? super Unit>, Object>(this.this$0, null){
                            int label;
                            /* synthetic */ Object L$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            {
                                this.this$0 = $receiver;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        long l;
                                        int n;
                                        int n2;
                                        int n3;
                                        TelemetryState telemetryState;
                                        TrackingMode trackingMode;
                                        Double d;
                                        LocationPoint locationPoint;
                                        TrackingUiState trackingUiState;
                                        AuthState authState;
                                        UiState uiState;
                                        UiState nextValue$iv;
                                        Object prevValue$iv;
                                        ResultKt.throwOnFailure((Object)object);
                                        TrackingTelemetry telemetry = (TrackingTelemetry)this.L$0;
                                        MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                                        boolean $i$f$update = false;
                                        do {
                                            prevValue$iv = $this$update$iv.getValue();
                                            UiState state = (UiState)prevValue$iv;
                                            boolean bl = false;
                                            Long l2 = Boxing.boxLong((long)telemetry.getLastLocationAtMs());
                                            long it = ((Number)l2).longValue();
                                            n = telemetry.getTrackingRestarts();
                                            n2 = telemetry.getRtdbWriteErrors();
                                            n3 = 0;
                                            telemetryState = state.getTracking().getTelemetry();
                                            trackingMode = null;
                                            d = null;
                                            locationPoint = null;
                                            trackingUiState = state.getTracking();
                                            authState = null;
                                            uiState = state;
                                            boolean bl2 = false;
                                            boolean bl3 = it > 0L;
                                            Long l3 = bl3 ? l2 : null;
                                            if (l3 != null) {
                                                l = l3;
                                                continue;
                                            }
                                            l = state.getTracking().getTelemetry().getLastLocationAtMs();
                                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(uiState, authState, TrackingUiState.copy$default(trackingUiState, false, false, false, locationPoint, d, trackingMode, false, false, TelemetryState.copy$default(telemetryState, n3, n2, n, l, telemetry.getLastTrackingRestartReason(), false, 0L, 0L, false, 0, 0, 2017, null), 255, null), null, null, null, null, null, null, 253, null))));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.L$0 = value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(TrackingTelemetry p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void ensureAuth() {
        String traceId = this.newTraceId("ensureAuth");
        TeamCompassViewModel.logActionStart$default(this, "ensureAuth", traceId, null, null, 12, null);
        this.authDelegate.ensureAuth((Function1<? super String, Unit>)((Function1)arg_0 -> TeamCompassViewModel.ensureAuth$lambda$17(this, traceId, arg_0)), (Function1<? super Throwable, Unit>)((Function1)arg_0 -> TeamCompassViewModel.ensureAuth$lambda$19(this, traceId, arg_0)));
    }

    private final void onAuthReady(String uid) {
        block1: {
            UiState it;
            UiState nextValue$iv;
            Object prevValue$iv;
            MutableStateFlow<UiState> $this$update$iv = this._ui;
            boolean $i$f$update = false;
            do {
                prevValue$iv = $this$update$iv.getValue();
                it = (UiState)prevValue$iv;
                boolean bl = false;
            } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, it.getAuth().copy(true, uid), null, null, null, null, null, null, null, 254, null))));
            String string2 = ((UiState)this._ui.getValue()).getTeamCode();
            if (string2 == null) break block1;
            String it2 = string2;
            boolean bl = false;
            this.startListening(it2);
        }
    }

    public final void setCallsign(@NotNull String value) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        Intrinsics.checkNotNullParameter((Object)value, (String)"value");
        String callsign = StringsKt.take((String)value, (int)24);
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, TeamUiState.copy$default(it.getTeam(), callsign, null, null, null, null, 0L, null, null, false, 510, null), null, null, null, null, null, 251, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, callsign, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ String $callsign;
            {
                this.this$0 = $receiver;
                this.$callsign = $callsign;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).setCallsign(this.$callsign, (Continuation<? super Unit>)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void setDefaultMode(@NotNull TrackingMode mode) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        Intrinsics.checkNotNullParameter((Object)mode, (String)"mode");
        boolean wasTracking = ((UiState)this._ui.getValue()).isTracking();
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, TrackingUiState.copy$default(it.getTracking(), false, false, false, null, null, mode, false, false, null, 479, null), null, null, null, null, null, null, 253, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, mode, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ TrackingMode $mode;
            {
                this.this$0 = $receiver;
                this.$mode = $mode;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).setDefaultMode(this.$mode, (Continuation<? super Unit>)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        if (wasTracking) {
            this.restartTracking();
        }
    }

    public final void setTeamViewMode(@NotNull TeamViewMode mode) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        Intrinsics.checkNotNullParameter((Object)((Object)mode), (String)"mode");
        TeamViewMode previous = ((UiState)this._ui.getValue()).getViewMode();
        if (previous == mode) {
            return;
        }
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, TeamUiState.copy$default(it.getTeam(), null, null, null, null, null, 0L, null, mode, false, 383, null), null, null, null, null, null, 251, null))));
        String teamCode = ((UiState)this._ui.getValue()).getTeamCode();
        CharSequence charSequence = teamCode;
        if (!(charSequence == null || StringsKt.isBlank((CharSequence)charSequence))) {
            this.startListening(teamCode);
        }
    }

    public final void setGamePolicy(int intervalSec, int distanceM) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        int safeInterval = RangesKt.coerceIn((int)intervalSec, (int)3, (int)20);
        int safeDistance = RangesKt.coerceIn((int)distanceM, (int)1, (int)100);
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), safeInterval, safeDistance, 0, 0, false, 0.0f, null, false, null, false, false, 2044, null), null, null, 223, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, safeInterval, safeDistance, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ int $safeInterval;
            final /* synthetic */ int $safeDistance;
            {
                this.this$0 = $receiver;
                this.$safeInterval = $safeInterval;
                this.$safeDistance = $safeDistance;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).setGamePolicy(this.$safeInterval, this.$safeDistance, (Continuation<? super Unit>)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        if (((UiState)this._ui.getValue()).isTracking() && ((UiState)this._ui.getValue()).getDefaultMode() == TrackingMode.GAME) {
            this.restartTracking();
        }
    }

    public final void setSilentPolicy(int intervalSec, int distanceM) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        int safeInterval = RangesKt.coerceIn((int)intervalSec, (int)10, (int)60);
        int safeDistance = RangesKt.coerceIn((int)distanceM, (int)1, (int)200);
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, safeInterval, safeDistance, false, 0.0f, null, false, null, false, false, 2035, null), null, null, 223, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, safeInterval, safeDistance, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ int $safeInterval;
            final /* synthetic */ int $safeDistance;
            {
                this.this$0 = $receiver;
                this.$safeInterval = $safeInterval;
                this.$safeDistance = $safeDistance;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).setSilentPolicy(this.$safeInterval, this.$safeDistance, (Continuation<? super Unit>)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        if (((UiState)this._ui.getValue()).isTracking() && ((UiState)this._ui.getValue()).getDefaultMode() == TrackingMode.SILENT) {
            this.restartTracking();
        }
    }

    public final void setThemeMode(@NotNull ThemeMode mode) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        Intrinsics.checkNotNullParameter((Object)((Object)mode), (String)"mode");
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0f, mode, false, null, false, false, 1983, null), null, null, 223, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, mode, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ ThemeMode $mode;
            {
                this.this$0 = $receiver;
                this.$mode = $mode;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).setThemeMode(this.$mode, (Continuation<? super Unit>)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void setLocationPermission(boolean granted) {
        UiState state;
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, TrackingUiState.copy$default(it.getTracking(), false, granted, false, null, null, null, false, false, null, 509, null), null, null, null, null, null, null, 253, null))));
        $this$update$iv = this._ui;
        $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            state = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = this.locationReadinessCoordinator.applyServiceState(state))));
        if (!granted) {
            this.stopTracking();
            return;
        }
        this.refreshLocationPreview();
    }

    public final void refreshLocationReadiness() {
        LocationReadinessUpdate update;
        UiState nextValue$iv;
        Object prevValue$iv;
        String permissionError = this.tr(R.string.vm_error_location_permission_required, new Object[0]);
        String servicesDisabledError = this.tr(R.string.vm_error_location_services_disabled, new Object[0]);
        String trackingDisabledError = this.tr(R.string.vm_error_location_disabled_during_tracking, new Object[0]);
        boolean shouldRefreshPreview = false;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            UiState state = (UiState)prevValue$iv;
            boolean bl = false;
            update = this.locationReadinessCoordinator.refreshReadiness(state, permissionError, servicesDisabledError, trackingDisabledError);
            shouldRefreshPreview = update.getShouldRefreshPreview();
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = update.getUpdatedState())));
        if (shouldRefreshPreview) {
            this.refreshLocationPreview();
        }
    }

    private final void bindSavedStateHandle() {
        SavedStateHandle savedStateHandle = this.savedStateHandle;
        if (savedStateHandle == null) {
            return;
        }
        SavedStateHandle handle = savedStateHandle;
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, handle, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ SavedStateHandle $handle;
            {
                this.this$0 = $receiver;
                this.$handle = $handle;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        Flow $this$map$iv = (Flow)this.this$0.getUi();
                        boolean $i$f$map = false;
                        Flow $this$unsafeTransform$iv$iv = $this$map$iv;
                        boolean $i$f$unsafeTransform = false;
                        boolean $i$f$unsafeFlow = false;
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest((Flow)FlowKt.distinctUntilChanged((Flow)((Flow)new Flow<RestorableVmState>($this$unsafeTransform$iv$iv){
                            final /* synthetic */ Flow $this_unsafeTransform$inlined;
                            {
                                this.$this_unsafeTransform$inlined = flow;
                            }

                            public Object collect(FlowCollector collector, Continuation $completion) {
                                Continuation continuation = $completion;
                                FlowCollector $this$unsafeTransform_u24lambda_u240 = collector;
                                boolean bl = false;
                                Object object = this.$this_unsafeTransform$inlined.collect(new FlowCollector($this$unsafeTransform_u24lambda_u240){
                                    final /* synthetic */ FlowCollector $this_unsafeFlow;
                                    {
                                        this.$this_unsafeFlow = $receiver;
                                    }

                                    /*
                                     * Unable to fully structure code
                                     */
                                    public final Object emit(Object var1_1, Continuation $completion) {
                                        if (!($completion instanceof bindSavedStateHandle$1$invokeSuspend$$inlined$map$1$2$1)) ** GOTO lbl-1000
                                        var3_3 = $completion;
                                        if ((var3_3.label & -2147483648) != 0) {
                                            var3_3.label -= -2147483648;
                                        } else lbl-1000:
                                        // 2 sources

                                        {
                                            $continuation = new ContinuationImpl(this, $completion){
                                                /* synthetic */ Object result;
                                                int label;
                                                Object L$0;
                                                final /* synthetic */ bindSavedStateHandle$1$invokeSuspend$$inlined$map$1$2 this$0;
                                                {
                                                    this.this$0 = this$0;
                                                    super($completion);
                                                }

                                                public final Object invokeSuspend(Object $result) {
                                                    this.result = $result;
                                                    this.label |= Integer.MIN_VALUE;
                                                    return this.this$0.emit(null, (Continuation)this);
                                                }
                                            };
                                        }
                                        $result = $continuation.result;
                                        var5_5 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                        switch ($continuation.label) {
                                            case 0: {
                                                ResultKt.throwOnFailure((Object)$result);
                                                var6_6 = value;
                                                $this$map_u24lambda_u245 = this.$this_unsafeFlow;
                                                $i$a$-unsafeTransform-FlowKt__TransformKt$map$1 = false;
                                                var9_10 = $this$map_u24lambda_u245;
                                                (Continuation)$continuation;
                                                state = (UiState)value;
                                                $i$a$-map-TeamCompassViewModel$bindSavedStateHandle$1$1 = false;
                                                $continuation.label = 1;
                                                v0 = var9_10.emit((Object)new RestorableVmState(state.getTeamCode(), state.getDefaultMode(), state.getPlayerMode(), state.isTracking(), state.getMySosUntilMs()), (Continuation)$continuation);
                                                if (v0 == var5_5) {
                                                    return var5_5;
                                                }
                                                ** GOTO lbl30
                                            }
                                            case 1: {
                                                $i$a$-unsafeTransform-FlowKt__TransformKt$map$1 = false;
                                                ResultKt.throwOnFailure((Object)$result);
                                                v0 = $result;
lbl30:
                                                // 2 sources

                                                return Unit.INSTANCE;
                                            }
                                        }
                                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                                    }
                                }, $completion);
                                if (object == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
                                    return object;
                                }
                                return Unit.INSTANCE;
                            }
                        })), (Function2)((Function2)new Function2<RestorableVmState, Continuation<? super Unit>, Object>(this.$handle, null){
                            int label;
                            /* synthetic */ Object L$0;
                            final /* synthetic */ SavedStateHandle $handle;
                            {
                                this.$handle = $handle;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        ResultKt.throwOnFailure((Object)object);
                                        RestorableVmState snapshot = (RestorableVmState)this.L$0;
                                        CharSequence charSequence = snapshot.getTeamCode();
                                        if (charSequence == null || StringsKt.isBlank((CharSequence)charSequence)) {
                                            this.$handle.remove("state_team_code");
                                        } else {
                                            this.$handle.set("state_team_code", (Object)snapshot.getTeamCode());
                                        }
                                        this.$handle.set("state_default_mode", (Object)snapshot.getDefaultMode().name());
                                        this.$handle.set("state_player_mode", (Object)snapshot.getPlayerMode().name());
                                        this.$handle.set("state_is_tracking", (Object)Boxing.boxBoolean((boolean)snapshot.isTracking()));
                                        this.$handle.set("state_my_sos_until_ms", (Object)Boxing.boxLong((long)snapshot.getMySosUntilMs()));
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.L$0 = value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(RestorableVmState p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void refreshLocationPreview() {
        UiState state;
        UiState nextValue$iv;
        Object prevValue$iv;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            state = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = this.locationReadinessCoordinator.applyServiceState(state))));
        if (!this.locationReadinessCoordinator.hasLocationPermission()) {
            return;
        }
        this.fusedPreview.getLastLocation().addOnSuccessListener(arg_0 -> TeamCompassViewModel.refreshLocationPreview$lambda$34(arg_0 -> TeamCompassViewModel.refreshLocationPreview$lambda$33(this, arg_0), arg_0)).addOnFailureListener(TeamCompassViewModel::refreshLocationPreview$lambda$35);
    }

    public final void togglePlayerMode() {
        PlayerMode next = ((UiState)this._ui.getValue()).getPlayerMode() == PlayerMode.GAME ? PlayerMode.DEAD : PlayerMode.GAME;
        this.setPlayerMode(next);
    }

    public final void setPlayerMode(@NotNull PlayerMode mode) {
        TeamUiState teamUiState;
        TrackingUiState trackingUiState;
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        Intrinsics.checkNotNullParameter((Object)mode, (String)"mode");
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
            teamUiState = TeamUiState.copy$default(it.getTeam(), null, null, null, null, mode, 0L, null, null, false, 495, null);
            trackingUiState = TrackingUiState.copy$default(it.getTracking(), false, false, false, null, null, null, false, false, null, 383, null);
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, trackingUiState, teamUiState, null, null, null, null, null, 249, null))));
        if (mode == PlayerMode.DEAD) {
            this.startDeadReminder();
        } else {
            this.stopDeadReminder();
        }
        this.trackingController.updateStatus(mode, ((UiState)this._ui.getValue()).getMySosUntilMs(), true);
    }

    public final void setEnemyMarkEnabled(boolean enabled) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, MapUiState.copy$default(it.getMap(), null, null, null, null, enabled, null, null, false, 0.0f, 495, null), null, null, null, null, 247, null))));
    }

    public final void setTargetPreset(@NotNull TargetFilterPreset preset) {
        Intrinsics.checkNotNullParameter((Object)preset, (String)"preset");
        this.updateTargetFilterStateByUser((Function1<? super TargetFilterState, TargetFilterState>)((Function1)arg_0 -> TeamCompassViewModel.setTargetPreset$lambda$38(preset, arg_0)));
    }

    public final void setNearRadius(int radiusM) {
        int safeRadius = RangesKt.coerceIn((int)radiusM, (int)50, (int)500);
        this.updateTargetFilterStateByUser((Function1<? super TargetFilterState, TargetFilterState>)((Function1)arg_0 -> TeamCompassViewModel.setNearRadius$lambda$39(safeRadius, arg_0)));
    }

    public final void setShowDead(boolean showDead) {
        this.updateTargetFilterStateByUser((Function1<? super TargetFilterState, TargetFilterState>)((Function1)arg_0 -> TeamCompassViewModel.setShowDead$lambda$40(showDead, arg_0)));
    }

    public final void setShowStale(boolean showStale) {
        this.updateTargetFilterStateByUser((Function1<? super TargetFilterState, TargetFilterState>)((Function1)arg_0 -> TeamCompassViewModel.setShowStale$lambda$41(showStale, arg_0)));
    }

    public final void setFocusMode(boolean enabled) {
        this.updateTargetFilterStateByUser((Function1<? super TargetFilterState, TargetFilterState>)((Function1)arg_0 -> TeamCompassViewModel.setFocusMode$lambda$42(enabled, arg_0)));
    }

    private final void updateTargetFilterStateByUser(Function1<? super TargetFilterState, TargetFilterState> transform) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        if (!this.tacticalFiltersEnabled) {
            return;
        }
        this.targetFilterDirtyByUser = true;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, FilterUiState.copy$default(it.getFilter(), (TargetFilterState)transform.invoke((Object)it.getFilter().getTargetFilterState()), null, null, 6, null), null, null, null, 239, null))));
        TeamCompassViewModel.refreshTargetsFromState$default(this, 0L, 1, null);
    }

    public final void importTacticalMap(@NotNull Uri uri) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        Intrinsics.checkNotNullParameter((Object)uri, (String)"uri");
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, TeamUiState.copy$default(it.getTeam(), null, null, null, null, null, 0L, null, null, true, 255, null), null, null, null, null, null, 251, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, uri, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ Uri $uri;
            {
                this.this$0 = $receiver;
                this.$uri = $uri;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object var1_1) {
                var10_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)var1_1);
                        this.label = 1;
                        v0 = TeamCompassViewModel.access$getMapCoordinator$p(this.this$0).importMap(TeamCompassViewModel.access$getApplication$p(this.this$0), this.$uri, (Continuation<? super TacticalMap>)((Continuation)this));
                        ** if (v0 != var10_2) goto lbl11
lbl10:
                        // 1 sources

                        return var10_2;
lbl11:
                        // 1 sources

                        ** GOTO lbl17
                    }
                    case 1: {
                        try {
                            ResultKt.throwOnFailure((Object)$result);
                            v0 = $result;
lbl17:
                            // 2 sources

                            map = (TacticalMap)v0;
                            $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                            $i$f$update = false;
                            do {
                                prevValue$iv = $this$update$iv.getValue();
                                it = (UiState)prevValue$iv;
                                $i$a$-update-TeamCompassViewModel$importTacticalMap$2$1 = false;
                                var8_15 = MapUiState.copy$default(it.getMap(), null, null, null, null, false, null, map, true, 0.0f, 319, null);
                                var9_16 = TeamUiState.copy$default(it.getTeam(), null, null, null, null, null, 0L, null, null, false, 255, null);
                            } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, var9_16, var8_15, null, null, null, null, 243, null))));
                        }
                        catch (Exception e) {
                            this.this$0.emitError(TeamCompassViewModel.access$tr(this.this$0, R.string.vm_error_kmz_load_failed, new Object[0]), e);
                            $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                            $i$f$update = false;
                            do {
                                prevValue$iv = $this$update$iv.getValue();
                                it = (UiState)prevValue$iv;
                                $i$a$-update-TeamCompassViewModel$importTacticalMap$2$2 = false;
                            } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, TeamUiState.copy$default(it.getTeam(), null, null, null, null, null, 0L, null, null, false, 255, null), null, null, null, null, null, 251, null))));
                        }
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void clearTacticalMap() {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, MapUiState.copy$default(it.getMap(), null, null, null, null, false, null, null, false, 0.0f, 319, null), null, null, null, null, 247, null))));
    }

    public final void setMapEnabled(boolean enabled) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, MapUiState.copy$default(it.getMap(), null, null, null, null, false, null, null, enabled, 0.0f, 383, null), null, null, null, null, 247, null))));
    }

    public final void setMapOpacity(float opacity) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, MapUiState.copy$default(it.getMap(), null, null, null, null, false, null, null, false, RangesKt.coerceIn((float)opacity, (float)0.0f, (float)1.0f), 255, null), null, null, null, null, 247, null))));
    }

    public final void saveMapChangesToSource(@NotNull List<KmlPoint> newPoints, @NotNull List<KmlPoint> deletedPoints) {
        Intrinsics.checkNotNullParameter(newPoints, (String)"newPoints");
        Intrinsics.checkNotNullParameter(deletedPoints, (String)"deletedPoints");
        if (!this.mapCoordinator.hasPointChanges(newPoints, deletedPoints)) {
            return;
        }
        TacticalMap map = ((UiState)this._ui.getValue()).getActiveMap();
        if (map == null) {
            TeamCompassViewModel.emitError$default(this, this.tr(R.string.map_not_loaded, new Object[0]), null, 2, null);
            return;
        }
        Uri sourceUri = this.mapCoordinator.sourceUriOrNull(map);
        if (sourceUri == null) {
            TeamCompassViewModel.emitError$default(this, this.tr(R.string.vm_error_map_source_no_access, new Object[0]), null, 2, null);
            return;
        }
        this.saveMapChangesInternal(sourceUri, map, newPoints, deletedPoints);
    }

    public static /* synthetic */ void saveMapChangesToSource$default(TeamCompassViewModel teamCompassViewModel, List list, List list2, int n, Object object) {
        if ((n & 2) != 0) {
            list2 = CollectionsKt.emptyList();
        }
        teamCompassViewModel.saveMapChangesToSource(list, list2);
    }

    public final void saveMapChangesAs(@NotNull Uri uri, @NotNull List<KmlPoint> newPoints, @NotNull List<KmlPoint> deletedPoints) {
        Intrinsics.checkNotNullParameter((Object)uri, (String)"uri");
        Intrinsics.checkNotNullParameter(newPoints, (String)"newPoints");
        Intrinsics.checkNotNullParameter(deletedPoints, (String)"deletedPoints");
        if (!this.mapCoordinator.hasPointChanges(newPoints, deletedPoints)) {
            return;
        }
        TacticalMap map = ((UiState)this._ui.getValue()).getActiveMap();
        if (map == null) {
            TeamCompassViewModel.emitError$default(this, this.tr(R.string.map_not_loaded, new Object[0]), null, 2, null);
            return;
        }
        this.saveMapChangesInternal(uri, map, newPoints, deletedPoints);
    }

    public static /* synthetic */ void saveMapChangesAs$default(TeamCompassViewModel teamCompassViewModel, Uri uri, List list, List list2, int n, Object object) {
        if ((n & 4) != 0) {
            list2 = CollectionsKt.emptyList();
        }
        teamCompassViewModel.saveMapChangesAs(uri, list, list2);
    }

    private final void saveMapChangesInternal(Uri destinationUri, TacticalMap map, List<KmlPoint> newPoints, List<KmlPoint> deletedPoints) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, TeamUiState.copy$default(it.getTeam(), null, null, null, null, null, 0L, null, null, true, 255, null), null, null, null, null, null, 251, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, map, newPoints, deletedPoints, destinationUri, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ TacticalMap $map;
            final /* synthetic */ List<KmlPoint> $newPoints;
            final /* synthetic */ List<KmlPoint> $deletedPoints;
            final /* synthetic */ Uri $destinationUri;
            {
                this.this$0 = $receiver;
                this.$map = $map;
                this.$newPoints = $newPoints;
                this.$deletedPoints = $deletedPoints;
                this.$destinationUri = $destinationUri;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object var1_1) {
                var16_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)var1_1);
                        this.label = 1;
                        v0 = TeamCompassViewModel.access$getMapCoordinator$p(this.this$0).saveChanges(TeamCompassViewModel.access$getApplication$p(this.this$0), this.$map, this.$newPoints, this.$deletedPoints, this.$destinationUri, (Continuation<? super Unit>)((Continuation)this));
                        ** if (v0 != var16_2) goto lbl11
lbl10:
                        // 1 sources

                        return var16_2;
lbl11:
                        // 1 sources

                        ** GOTO lbl17
                    }
                    case 1: {
                        try {
                            ResultKt.throwOnFailure((Object)$result);
                            v0 = $result;
lbl17:
                            // 2 sources

                            var2_3 = TeamCompassViewModel.access$get_ui$p(this.this$0);
                            var3_5 = this.$map;
                            var4_7 = this.this$0;
                            var5_9 = this.$newPoints;
                            var6_11 = this.$deletedPoints;
                            $i$f$update = false;
                            do {
                                prevValue$iv = $this$update$iv.getValue();
                                state = (UiState)prevValue$iv;
                                $i$a$-update-TeamCompassViewModel$saveMapChangesInternal$2$1 = false;
                                active = state.getMap().getActiveMap();
                                if (active == null || !Intrinsics.areEqual((Object)active.getId(), (Object)var3_5.getId())) {
                                    v1 = UiState.copy$default(state, null, null, TeamUiState.copy$default(state.getTeam(), null, null, null, null, null, 0L, null, null, false, 255, null), null, null, null, null, null, 251, null);
                                    continue;
                                }
                                mergedMap = TeamCompassViewModel.access$getMapCoordinator$p(var4_7).mergePoints(active, var5_9, var6_11);
                                var13_20 = MapUiState.copy$default(state.getMap(), null, null, null, null, false, null, mergedMap, false, 0.0f, 447, null);
                                var14_21 = TeamUiState.copy$default(state.getTeam(), null, null, null, null, null, 0L, null, null, false, 255, null);
                                v1 = UiState.copy$default(state, null, null, var14_21, var13_20, null, null, null, null, 243, null);
                            } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = v1)));
                        }
                        catch (Exception e) {
                            this.this$0.emitError(TeamCompassViewModel.access$tr(this.this$0, R.string.vm_error_file_write_failed, new Object[0]), e);
                            $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                            $i$f$update = false;
                            do {
                                prevValue$iv = $this$update$iv.getValue();
                                it = (UiState)prevValue$iv;
                                $i$a$-update-TeamCompassViewModel$saveMapChangesInternal$2$2 = false;
                            } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, TeamUiState.copy$default(it.getTeam(), null, null, null, null, null, 0L, null, null, false, 255, null), null, null, null, null, null, 251, null))));
                        }
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void toggleSos() {
        long now = System.currentTimeMillis();
        if (((UiState)this._ui.getValue()).getMySosUntilMs() > now) {
            this.clearSos();
        } else {
            this.triggerSos();
        }
    }

    public final void triggerSos() {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        long until = System.currentTimeMillis() + 60000L;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, TeamUiState.copy$default(it.getTeam(), null, null, null, null, null, until, null, null, false, 479, null), null, null, null, null, null, 251, null))));
        this.trackingController.updateStatus(((UiState)this._ui.getValue()).getPlayerMode(), until, true);
    }

    public final void clearSos() {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, TeamUiState.copy$default(it.getTeam(), null, null, null, null, null, 0L, null, null, false, 479, null), null, null, null, null, null, 251, null))));
        this.trackingController.updateStatus(((UiState)this._ui.getValue()).getPlayerMode(), 0L, true);
    }

    public final void addPointHere(@NotNull String label, @NotNull String icon, boolean forTeam) {
        Intrinsics.checkNotNullParameter((Object)label, (String)"label");
        Intrinsics.checkNotNullParameter((Object)icon, (String)"icon");
        LocationPoint locationPoint = ((UiState)this._ui.getValue()).getMe();
        if (locationPoint == null) {
            return;
        }
        LocationPoint me = locationPoint;
        this.addPointAt(me.getLat(), me.getLon(), label, icon, forTeam);
    }

    public final void addPointAt(double lat, double lon, @NotNull String label, @NotNull String icon, boolean forTeam) {
        Intrinsics.checkNotNullParameter((Object)label, (String)"label");
        Intrinsics.checkNotNullParameter((Object)icon, (String)"icon");
        this.getTacticalActionsCoordinator().addPointAt(lat, lon, label, icon, forTeam, this.tr(R.string.vm_error_add_point_failed, new Object[0]), this.tr(R.string.error_invalid_input, new Object[0]));
    }

    public final void updatePoint(@NotNull String id, double lat, double lon, @NotNull String label, @NotNull String icon, boolean isTeam) {
        Intrinsics.checkNotNullParameter((Object)id, (String)"id");
        Intrinsics.checkNotNullParameter((Object)label, (String)"label");
        Intrinsics.checkNotNullParameter((Object)icon, (String)"icon");
        this.getTacticalActionsCoordinator().updatePoint(id, lat, lon, label, icon, isTeam, this.tr(R.string.vm_error_only_author_edit_team_point, new Object[0]), this.tr(R.string.vm_error_update_point_failed, new Object[0]), this.tr(R.string.error_invalid_input, new Object[0]));
    }

    public final void deletePoint(@NotNull String id, boolean isTeam) {
        Intrinsics.checkNotNullParameter((Object)id, (String)"id");
        this.getTacticalActionsCoordinator().deletePoint(id, isTeam, this.tr(R.string.vm_error_only_author_delete_team_point, new Object[0]), this.tr(R.string.vm_error_delete_point_failed, new Object[0]));
    }

    public final void sendQuickCommand(@NotNull QuickCommandType type) {
        Intrinsics.checkNotNullParameter((Object)((Object)type), (String)"type");
        this.getTacticalActionsCoordinator().sendQuickCommand(type);
    }

    public final void addEnemyPing(double lat, double lon, @NotNull QuickCommandType type) {
        Intrinsics.checkNotNullParameter((Object)((Object)type), (String)"type");
        this.getTacticalActionsCoordinator().addEnemyPing(lat, lon, type, this.tr(R.string.vm_error_enemy_mark_failed, new Object[0]), this.tr(R.string.error_invalid_input, new Object[0]));
    }

    public final void assignTeamMemberRole(@NotNull String targetUid, @NotNull TeamRolePatch patch) {
        Intrinsics.checkNotNullParameter((Object)targetUid, (String)"targetUid");
        Intrinsics.checkNotNullParameter((Object)patch, (String)"patch");
        UiState state = (UiState)this._ui.getValue();
        String string2 = state.getTeamCode();
        if (string2 == null) {
            return;
        }
        String code = string2;
        String string3 = state.getUid();
        if (string3 == null) {
            return;
        }
        String actorUid = string3;
        if (StringsKt.isBlank((CharSequence)targetUid)) {
            return;
        }
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, code, actorUid, targetUid, patch, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ String $code;
            final /* synthetic */ String $actorUid;
            final /* synthetic */ String $targetUid;
            final /* synthetic */ TeamRolePatch $patch;
            {
                this.this$0 = $receiver;
                this.$code = $code;
                this.$actorUid = $actorUid;
                this.$targetUid = $targetUid;
                this.$patch = $patch;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object var1_1) {
                var3_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)var1_1);
                        this.label = 1;
                        v0 = TeamCompassViewModel.access$getTeamRepository$p(this.this$0).assignTeamMemberRole(this.$code, this.$actorUid, this.$targetUid, this.$patch, (Continuation<? super TeamActionResult<TeamMemberRoleProfile>>)((Continuation)this));
                        if (v0 == var3_2) {
                            return var3_2;
                        }
                        ** GOTO lbl13
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        v0 = $result;
lbl13:
                        // 2 sources

                        result = (TeamActionResult)v0;
                        if (!(result instanceof TeamActionResult.Success)) {
                            if (result instanceof TeamActionResult.Failure) {
                                TeamCompassViewModel.access$handleActionFailure(this.this$0, TeamCompassViewModel.access$tr(this.this$0, R.string.vm_error_assign_role_failed, new Object[0]), ((TeamActionResult.Failure)result).getDetails());
                            } else {
                                throw new NoWhenBranchMatchedException();
                            }
                        }
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void assignTeamMemberRolesBulk(@NotNull List<String> targetUids, @NotNull TeamRolePatch patch) {
        Intrinsics.checkNotNullParameter(targetUids, (String)"targetUids");
        Intrinsics.checkNotNullParameter((Object)patch, (String)"patch");
        UiState state = (UiState)this._ui.getValue();
        String string2 = state.getTeamCode();
        if (string2 == null) {
            return;
        }
        String code = string2;
        String string3 = state.getUid();
        if (string3 == null) {
            return;
        }
        String actorUid = string3;
        if (patch.getCommandRole() == null && patch.getCombatRole() == null && patch.getVehicleRole() == null && patch.getOrgPath() == null) {
            return;
        }
        List targets = SequencesKt.toList((Sequence)SequencesKt.distinct((Sequence)SequencesKt.filter((Sequence)SequencesKt.map((Sequence)CollectionsKt.asSequence((Iterable)targetUids), TeamCompassViewModel::assignTeamMemberRolesBulk$lambda$51), arg_0 -> TeamCompassViewModel.assignTeamMemberRolesBulk$lambda$52(actorUid, arg_0))));
        if (targets.isEmpty()) {
            return;
        }
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>((List<String>)targets, this, code, actorUid, patch, null){
            Object L$0;
            Object L$1;
            Object L$2;
            Object L$3;
            Object L$4;
            Object L$5;
            Object L$6;
            Object L$7;
            int label;
            final /* synthetic */ List<String> $targets;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ String $code;
            final /* synthetic */ String $actorUid;
            final /* synthetic */ TeamRolePatch $patch;
            {
                this.$targets = $targets;
                this.this$0 = $receiver;
                this.$code = $code;
                this.$actorUid = $actorUid;
                this.$patch = $patch;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object var1_1) {
                var16_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)var1_1);
                        successCount = new Ref.IntRef();
                        failedCount = new Ref.IntRef();
                        firstFailure = new Ref.ObjectRef();
                        var5_6 = this.$targets;
                        var6_7 = this.this$0;
                        var7_8 = this.$code;
                        var8_9 = this.$actorUid;
                        var9_10 = this.$patch;
                        $i$f$forEach = false;
                        var11_12 = $this$forEach$iv.iterator();
lbl15:
                        // 3 sources

                        while (var11_12.hasNext()) {
                            element$iv = var11_12.next();
                            targetUid = (String)element$iv;
                            $i$a$-forEach-TeamCompassViewModel$assignTeamMemberRolesBulk$1$1 = false;
                            this.L$0 = successCount;
                            this.L$1 = failedCount;
                            this.L$2 = firstFailure;
                            this.L$3 = var6_7;
                            this.L$4 = var7_8;
                            this.L$5 = var8_9;
                            this.L$6 = var9_10;
                            this.L$7 = var11_12;
                            this.label = 1;
                            v0 = TeamCompassViewModel.access$getTeamRepository$p((TeamCompassViewModel)var6_7).assignTeamMemberRole(var7_8, var8_9, targetUid, var9_10, (Continuation<? super TeamActionResult<TeamMemberRoleProfile>>)this);
                            if (v0 == var16_2) {
                                return var16_2;
                            }
                            ** GOTO lbl46
                        }
                        break;
                    }
                    case 1: {
                        $i$f$forEach = false;
                        $i$a$-forEach-TeamCompassViewModel$assignTeamMemberRolesBulk$1$1 = false;
                        var11_12 = (Iterator<T>)this.L$7;
                        var9_10 = (TeamRolePatch)this.L$6;
                        var8_9 = (String)this.L$5;
                        var7_8 = (String)this.L$4;
                        var6_7 = (TeamCompassViewModel)this.L$3;
                        firstFailure = (Ref.ObjectRef)this.L$2;
                        failedCount = (Ref.IntRef)this.L$1;
                        successCount = (Ref.IntRef)this.L$0;
                        ResultKt.throwOnFailure((Object)$result);
                        v0 = $result;
lbl46:
                        // 2 sources

                        result = (TeamActionResult)v0;
                        if (!(result instanceof TeamActionResult.Success)) ** GOTO lbl51
                        ++successCount.element;
                        ** GOTO lbl15
lbl51:
                        // 1 sources

                        if (!(result instanceof TeamActionResult.Failure)) ** GOTO lbl56
                        ++failedCount.element;
                        if (firstFailure.element == null) {
                            firstFailure.element = ((TeamActionResult.Failure)result).getDetails();
                        }
                        ** GOTO lbl15
lbl56:
                        // 1 sources

                        throw new NoWhenBranchMatchedException();
                    }
                }
                if (failedCount.element > 0) {
                    if (successCount.element > 0) {
                        var6_7 = new Object[]{Boxing.boxInt((int)successCount.element), Boxing.boxInt((int)failedCount.element)};
                        v1 = TeamCompassViewModel.access$tr(this.this$0, R.string.vm_error_bulk_assign_partial_format, var6_7);
                    } else {
                        var6_7 = new Object[]{Boxing.boxInt((int)failedCount.element)};
                        v1 = TeamCompassViewModel.access$tr(this.this$0, R.string.vm_error_bulk_assign_failed_format, var6_7);
                    }
                    message = v1;
                    v2 = (TeamActionFailure)firstFailure.element;
                    this.this$0.emitError(message, v2 != null ? v2.getCause() : null);
                }
                return Unit.INSTANCE;
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void createTeam() {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        CharSequence charSequence;
        if (((UiState)this._ui.getValue()).isBusy()) {
            return;
        }
        String uid = ((UiState)this._ui.getValue()).getUid();
        CharSequence charSequence2 = uid;
        if (charSequence2 == null || StringsKt.isBlank((CharSequence)charSequence2)) {
            TeamCompassViewModel.emitError$default(this, this.tr(R.string.vm_error_auth_not_ready, new Object[0]), null, 2, null);
            this.ensureAuth();
            return;
        }
        CharSequence charSequence3 = ((UiState)this._ui.getValue()).getCallsign();
        if (StringsKt.isBlank((CharSequence)charSequence3)) {
            boolean bl = false;
            charSequence = this.tr(R.string.default_callsign_player, new Object[0]);
        } else {
            charSequence = charSequence3;
        }
        String callsign = (String)charSequence;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, TeamUiState.copy$default(it.getTeam(), null, null, null, null, null, 0L, null, null, true, 255, null), null, null, null, null, null, 251, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, uid, callsign, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ String $uid;
            final /* synthetic */ String $callsign;
            {
                this.this$0 = $receiver;
                this.$uid = $uid;
                this.$callsign = $callsign;
                super(2, $completion);
            }

            /*
             * Exception decompiling
             */
            public final Object invokeSuspend(Object var1_1) {
                /*
                 * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
                 * 
                 * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [6[CASE], 3[SWITCH]], but top level block is 2[TRYBLOCK]
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.dump(Method.java:598)
                 *     at org.benf.cfr.reader.entities.classfilehelpers.ClassFileDumperAnonymousInner.dumpWithArgs(ClassFileDumperAnonymousInner.java:87)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.ConstructorInvokationAnonymousInner.dumpInner(ConstructorInvokationAnonymousInner.java:82)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression.dumpWithOuterPrecedence(AbstractExpression.java:142)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression.dumpInner(CastExpression.java:114)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression.dumpWithOuterPrecedence(AbstractExpression.java:139)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression.dumpInner(CastExpression.java:114)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression.dumpWithOuterPrecedence(AbstractExpression.java:142)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression.dump(AbstractExpression.java:98)
                 *     at org.benf.cfr.reader.state.TypeUsageCollectingDumper.dump(TypeUsageCollectingDumper.java:194)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.StaticFunctionInvokation.dumpInner(StaticFunctionInvokation.java:143)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression.dumpWithOuterPrecedence(AbstractExpression.java:142)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression.dump(AbstractExpression.java:98)
                 *     at org.benf.cfr.reader.state.TypeUsageCollectingDumper.dump(TypeUsageCollectingDumper.java:194)
                 *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredExpressionStatement.dump(StructuredExpressionStatement.java:29)
                 *     at org.benf.cfr.reader.state.TypeUsageCollectingDumper.dump(TypeUsageCollectingDumper.java:194)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.dump(Op04StructuredStatement.java:220)
                 *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.Block.dump(Block.java:564)
                 *     at org.benf.cfr.reader.state.TypeUsageCollectingDumper.dump(TypeUsageCollectingDumper.java:194)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.dump(Op04StructuredStatement.java:220)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.dump(AttributeCode.java:135)
                 *     at org.benf.cfr.reader.state.TypeUsageCollectingDumper.dump(TypeUsageCollectingDumper.java:194)
                 *     at org.benf.cfr.reader.entities.Method.dump(Method.java:627)
                 *     at org.benf.cfr.reader.entities.classfilehelpers.AbstractClassFileDumper.dumpMethods(AbstractClassFileDumper.java:211)
                 *     at org.benf.cfr.reader.entities.classfilehelpers.ClassFileDumperNormal.dump(ClassFileDumperNormal.java:70)
                 *     at org.benf.cfr.reader.entities.ClassFile.dump(ClassFile.java:1167)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:952)
                 *     at org.benf.cfr.reader.Driver.doClass(Driver.java:84)
                 *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:78)
                 *     at org.benf.cfr.reader.Main.main(Main.java:54)
                 */
                throw new IllegalStateException("Decompilation failed");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void joinTeam(@NotNull String codeRaw, boolean alsoCreateMember) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        CharSequence charSequence;
        Intrinsics.checkNotNullParameter((Object)codeRaw, (String)"codeRaw");
        if (((UiState)this._ui.getValue()).isBusy()) {
            return;
        }
        String uid = ((UiState)this._ui.getValue()).getUid();
        CharSequence charSequence2 = uid;
        if (charSequence2 == null || StringsKt.isBlank((CharSequence)charSequence2)) {
            TeamCompassViewModel.emitError$default(this, this.tr(R.string.vm_error_auth_not_ready, new Object[0]), null, 2, null);
            this.ensureAuth();
            return;
        }
        String code = this.normalizeTeamCode(codeRaw);
        if (code == null) {
            TeamCompassViewModel.emitError$default(this, this.tr(R.string.join_code_error, new Object[0]), null, 2, null);
            return;
        }
        if (!this.joinRateLimiter.canAttempt(code)) {
            TeamCompassViewModel.emitError$default(this, this.tr(R.string.vm_error_join_rate_limited, new Object[0]), null, 2, null);
            return;
        }
        String traceId = this.newTraceId("joinTeam");
        this.logActionStart("joinTeam", traceId, code, uid);
        CharSequence charSequence3 = ((UiState)this._ui.getValue()).getCallsign();
        if (StringsKt.isBlank((CharSequence)charSequence3)) {
            boolean bl = false;
            charSequence = this.tr(R.string.default_callsign_player, new Object[0]);
        } else {
            charSequence = charSequence3;
        }
        String callsign = (String)charSequence;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, TeamUiState.copy$default(it.getTeam(), null, null, null, null, null, 0L, null, null, true, 255, null), null, null, null, null, null, 251, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, code, uid, callsign, alsoCreateMember, traceId, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ String $code;
            final /* synthetic */ String $uid;
            final /* synthetic */ String $callsign;
            final /* synthetic */ boolean $alsoCreateMember;
            final /* synthetic */ String $traceId;
            {
                this.this$0 = $receiver;
                this.$code = $code;
                this.$uid = $uid;
                this.$callsign = $callsign;
                this.$alsoCreateMember = $alsoCreateMember;
                this.$traceId = $traceId;
                super(2, $completion);
            }

            /*
             * Exception decompiling
             */
            public final Object invokeSuspend(Object var1_1) {
                /*
                 * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
                 * 
                 * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [6[CASE], 3[SWITCH]], but top level block is 2[TRYBLOCK]
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.dump(Method.java:598)
                 *     at org.benf.cfr.reader.entities.classfilehelpers.ClassFileDumperAnonymousInner.dumpWithArgs(ClassFileDumperAnonymousInner.java:87)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.ConstructorInvokationAnonymousInner.dumpInner(ConstructorInvokationAnonymousInner.java:82)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression.dumpWithOuterPrecedence(AbstractExpression.java:142)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression.dumpInner(CastExpression.java:114)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression.dumpWithOuterPrecedence(AbstractExpression.java:139)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression.dumpInner(CastExpression.java:114)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression.dumpWithOuterPrecedence(AbstractExpression.java:142)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression.dump(AbstractExpression.java:98)
                 *     at org.benf.cfr.reader.state.TypeUsageCollectingDumper.dump(TypeUsageCollectingDumper.java:194)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.StaticFunctionInvokation.dumpInner(StaticFunctionInvokation.java:143)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression.dumpWithOuterPrecedence(AbstractExpression.java:142)
                 *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression.dump(AbstractExpression.java:98)
                 *     at org.benf.cfr.reader.state.TypeUsageCollectingDumper.dump(TypeUsageCollectingDumper.java:194)
                 *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredExpressionStatement.dump(StructuredExpressionStatement.java:29)
                 *     at org.benf.cfr.reader.state.TypeUsageCollectingDumper.dump(TypeUsageCollectingDumper.java:194)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.dump(Op04StructuredStatement.java:220)
                 *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.Block.dump(Block.java:564)
                 *     at org.benf.cfr.reader.state.TypeUsageCollectingDumper.dump(TypeUsageCollectingDumper.java:194)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.dump(Op04StructuredStatement.java:220)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.dump(AttributeCode.java:135)
                 *     at org.benf.cfr.reader.state.TypeUsageCollectingDumper.dump(TypeUsageCollectingDumper.java:194)
                 *     at org.benf.cfr.reader.entities.Method.dump(Method.java:627)
                 *     at org.benf.cfr.reader.entities.classfilehelpers.AbstractClassFileDumper.dumpMethods(AbstractClassFileDumper.java:211)
                 *     at org.benf.cfr.reader.entities.classfilehelpers.ClassFileDumperNormal.dump(ClassFileDumperNormal.java:70)
                 *     at org.benf.cfr.reader.entities.ClassFile.dump(ClassFile.java:1167)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:952)
                 *     at org.benf.cfr.reader.Driver.doClass(Driver.java:84)
                 *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:78)
                 *     at org.benf.cfr.reader.Main.main(Main.java:54)
                 */
                throw new IllegalStateException("Decompilation failed");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public static /* synthetic */ void joinTeam$default(TeamCompassViewModel teamCompassViewModel, String string2, boolean bl, int n, Object object) {
        if ((n & 2) != 0) {
            bl = true;
        }
        teamCompassViewModel.joinTeam(string2, bl);
    }

    /*
     * Unable to fully structure code
     */
    private final Object onTeamJoined(String var1_1, Continuation<? super Unit> $completion) {
        if (!($completion instanceof onTeamJoined.1)) ** GOTO lbl-1000
        var9_3 = $completion;
        if ((var9_3.label & -2147483648) != 0) {
            var9_3.label -= -2147483648;
        } else lbl-1000:
        // 2 sources

        {
            $continuation = new ContinuationImpl(this, $completion){
                Object L$0;
                Object L$1;
                /* synthetic */ Object result;
                final /* synthetic */ TeamCompassViewModel this$0;
                int label;
                {
                    this.this$0 = this$0;
                    super($completion);
                }

                @Nullable
                public final Object invokeSuspend(@NotNull Object $result) {
                    this.result = $result;
                    this.label |= Integer.MIN_VALUE;
                    return TeamCompassViewModel.access$onTeamJoined(this.this$0, null, (Continuation)this);
                }
            };
        }
        $result = $continuation.result;
        var10_5 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch ($continuation.label) {
            case 0: {
                ResultKt.throwOnFailure((Object)$result);
                $this$update$iv = this._ui;
                $i$f$update = false;
                do {
                    prevValue$iv = $this$update$iv.getValue();
                    it = (UiState)prevValue$iv;
                    $i$a$-update-TeamCompassViewModel$onTeamJoined$2 = false;
                } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, TeamUiState.copy$default(it.getTeam(), null, code, null, null, null, 0L, null, null, false, 509, null), null, null, null, null, null, 123, null))));
                $continuation.L$0 = this;
                $continuation.L$1 = code;
                $continuation.label = 1;
                v0 = this.prefs.setTeamCode(code, (Continuation<? super Unit>)$continuation);
                if (v0 == var10_5) {
                    return var10_5;
                }
                ** GOTO lbl33
            }
            case 1: {
                code = (String)$continuation.L$1;
                this = (TeamCompassViewModel)$continuation.L$0;
                ResultKt.throwOnFailure((Object)$result);
                v0 = $result;
lbl33:
                // 2 sources

                this.startListening(code);
                this.evaluateIdentityLinkingEligibility(code);
                return Unit.INSTANCE;
            }
        }
        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
    }

    private final void evaluateIdentityLinkingEligibility(String teamCode) {
        block6: {
            Throwable throwable;
            Object $this$evaluateIdentityLinkingEligibility_u24lambda_u2458;
            String string2 = ((UiState)this._ui.getValue()).getUid();
            if (string2 == null) {
                return;
            }
            String uid = string2;
            if (Intrinsics.areEqual((Object)this.identityLinkingPromptTrackedUid, (Object)uid)) {
                return;
            }
            String traceId = this.newTraceId("identityLinkingEligibility");
            this.logActionStart("identityLinkingEligibility", traceId, teamCode, uid);
            TeamCompassViewModel teamCompassViewModel = this;
            try {
                $this$evaluateIdentityLinkingEligibility_u24lambda_u2458 = teamCompassViewModel;
                boolean bl = false;
                $this$evaluateIdentityLinkingEligibility_u24lambda_u2458 = Result.constructor-impl((Object)$this$evaluateIdentityLinkingEligibility_u24lambda_u2458.identityLinkingService.evaluateEligibility());
            }
            catch (Throwable bl) {
                $this$evaluateIdentityLinkingEligibility_u24lambda_u2458 = Result.constructor-impl((Object)ResultKt.createFailure((Throwable)bl));
            }
            teamCompassViewModel = $this$evaluateIdentityLinkingEligibility_u24lambda_u2458;
            if (Result.isSuccess-impl((Object)((Object)teamCompassViewModel))) {
                IdentityLinkingEligibility eligibility = (IdentityLinkingEligibility)((Object)teamCompassViewModel);
                boolean bl = false;
                if (eligibility.getShouldPrompt()) {
                    this.identityLinkingPromptTrackedUid = uid;
                    Log.i((String)TAG, (String)("Identity linking is eligible for uid=" + uid + " team=" + teamCode + " reason=" + eligibility.getReason()));
                }
                this.logActionSuccess("identityLinkingEligibility", traceId, teamCode, uid);
            }
            Throwable throwable2 = Result.exceptionOrNull-impl((Object)((Object)teamCompassViewModel));
            if (throwable2 == null) break block6;
            Throwable err = throwable = throwable2;
            boolean bl = false;
            this.logActionFailure("identityLinkingEligibility", traceId, err, err.getMessage(), teamCode, uid);
        }
    }

    public final void markCompassHelpSeen() {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0f, null, false, null, false, false, 1535, null), null, null, 223, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).setShowCompassHelpOnce(false, (Continuation<? super Unit>)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void markOnboardingSeen() {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0f, null, false, null, false, false, 1023, null), null, null, 223, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).setShowOnboardingOnce(false, (Continuation<? super Unit>)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void setControlLayoutEdit(boolean enabled) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0f, null, enabled, null, false, false, 1919, null), null, null, 223, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, enabled, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ boolean $enabled;
            {
                this.this$0 = $receiver;
                this.$enabled = $enabled;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).setControlLayoutEdit(this.$enabled, (Continuation<? super Unit>)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void setControlPosition(@NotNull CompassControlId id, @NotNull ControlPosition position) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        Intrinsics.checkNotNullParameter((Object)((Object)id), (String)"id");
        Intrinsics.checkNotNullParameter((Object)position, (String)"position");
        ControlPosition normalized = CompassControlLayoutKt.normalized(position);
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0f, null, false, MapsKt.plus(it.getSettings().getControlPositions(), (Pair)TuplesKt.to((Object)((Object)id), (Object)normalized)), false, false, 1791, null), null, null, 223, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, id, normalized, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ CompassControlId $id;
            final /* synthetic */ ControlPosition $normalized;
            {
                this.this$0 = $receiver;
                this.$id = $id;
                this.$normalized = $normalized;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).setControlPosition(this.$id, this.$normalized, (Continuation<? super Unit>)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void resetControlPositions() {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        Map<CompassControlId, ControlPosition> defaults = CompassControlLayoutKt.defaultCompassControlPositions();
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0f, null, false, defaults, false, false, 1791, null), null, null, 223, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).resetControlPositions((Continuation<? super Unit>)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void applyControlLayoutPreset(@NotNull ControlLayoutPreset preset) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        Intrinsics.checkNotNullParameter((Object)((Object)preset), (String)"preset");
        Map<CompassControlId, ControlPosition> positions = CompassControlLayoutKt.controlPositionsForPreset(preset);
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0f, null, false, positions, false, false, 1791, null), null, null, 223, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(positions, this, null){
            Object L$0;
            Object L$1;
            Object L$2;
            int label;
            final /* synthetic */ Map<CompassControlId, ControlPosition> $positions;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.$positions = $positions;
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object var1_1) {
                block5: {
                    var12_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                    block0 : switch (this.label) {
                        case 0: {
                            ResultKt.throwOnFailure((Object)var1_1);
                            var2_3 = (Iterable)CompassControlId.getEntries();
                            var3_4 = this.$positions;
                            var4_5 = this.this$0;
                            $i$f$forEach = false;
                            var6_7 = $this$forEach$iv.iterator();
lbl10:
                            // 4 sources

                            while (var6_7.hasNext()) {
                                element$iv = var6_7.next();
                                id = (CompassControlId)element$iv;
                                $i$a$-forEach-TeamCompassViewModel$applyControlLayoutPreset$2$1 = false;
                                if ((ControlPosition)var3_4.get((Object)id) == null) break block0;
                                $i$a$-let-TeamCompassViewModel$applyControlLayoutPreset$2$1$1 = false;
                                this.L$0 = var3_4;
                                this.L$1 = var4_5;
                                this.L$2 = var6_7;
                                this.label = 1;
                                v0 = TeamCompassViewModel.access$getPrefs$p(var4_5).setControlPosition(id, pos, (Continuation<? super Unit>)this);
                                if (v0 != var12_2) continue;
                                return var12_2;
                            }
                            break block5;
                        }
                        case 1: {
                            $i$f$forEach = false;
                            $i$a$-forEach-TeamCompassViewModel$applyControlLayoutPreset$2$1 = false;
                            $i$a$-let-TeamCompassViewModel$applyControlLayoutPreset$2$1$1 = false;
                            var6_7 = (Iterator<T>)this.L$2;
                            var4_5 = (TeamCompassViewModel)this.L$1;
                            var3_4 = (Map)this.L$0;
                            ResultKt.throwOnFailure((Object)$result);
                            v0 = $result;
                            ** GOTO lbl10
                        }
                    }
                    ** GOTO lbl10
                }
                return Unit.INSTANCE;
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void leaveTeam() {
        FilterUiState filterUiState;
        MapUiState mapUiState;
        TeamUiState teamUiState;
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        this.stopTracking();
        this.stopListening();
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
            teamUiState = TeamUiState.copy$default(it.getTeam(), null, null, CollectionsKt.emptyList(), CollectionsKt.emptyList(), null, 0L, null, null, false, 433, null);
            filterUiState = FilterUiState.copy$default(it.getFilter(), null, CollectionsKt.emptyList(), CollectionsKt.emptyList(), 1, null);
            mapUiState = MapUiState.copy$default(it.getMap(), CollectionsKt.emptyList(), CollectionsKt.emptyList(), CollectionsKt.emptyList(), CollectionsKt.emptyList(), false, null, null, false, 0.0f, 496, null);
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, teamUiState, mapUiState, filterUiState, null, null, null, 227, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).setTeamCode(null, (Continuation<? super Unit>)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    private final void startListening(String codeRaw) {
        if (!((UiState)this._ui.getValue()).isAuthReady()) {
            return;
        }
        String string2 = ((UiState)this._ui.getValue()).getUid();
        if (string2 == null) {
            return;
        }
        String uid = string2;
        String traceId = this.newTraceId("startListening");
        this.logActionStart("startListening", traceId, codeRaw, uid);
        String code = this.normalizeTeamCode(codeRaw);
        if (code == null) {
            TeamCompassViewModel.emitError$default(this, this.tr(R.string.vm_error_team_code_invalid, new Object[0]), null, 2, null);
            TeamCompassViewModel.logActionFailure$default(this, "startListening", traceId, null, "invalid team code", codeRaw, uid, 4, null);
            this.stopListening();
            return;
        }
        Job job = this.teamObserverJob;
        if (job != null) {
            Job.DefaultImpls.cancel$default((Job)job, null, (int)1, null);
        }
        this.teamObserverJob = BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, code, uid, traceId, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ String $code;
            final /* synthetic */ String $uid;
            final /* synthetic */ String $traceId;
            {
                this.this$0 = $receiver;
                this.$code = $code;
                this.$uid = $uid;
                this.$traceId = $traceId;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object var1_1) {
                var6_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)var1_1);
                        var3_3 = ((UiState)TeamCompassViewModel.access$get_ui$p(this.this$0).getValue()).getCallsign();
                        var4_4 = this.this$0;
                        if (StringsKt.isBlank((CharSequence)var3_3)) {
                            $i$a$-ifBlank-TeamCompassViewModel$startListening$1$callsign$1 = false;
                            v0 = TeamCompassViewModel.access$tr(var4_4, R.string.default_callsign_player, new Object[0]);
                        } else {
                            v0 = var3_3;
                        }
                        callsign = (String)v0;
                        this.label = 1;
                        v1 = TeamCompassViewModel.access$getTeamSessionDelegate$p(this.this$0).preflightStartListening(this.$code, this.$uid, callsign, (Continuation<? super TeamListeningPreflightResult>)((Continuation)this));
                        if (v1 == var6_2) {
                            return var6_2;
                        }
                        ** GOTO lbl21
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        v1 = $result;
lbl21:
                        // 2 sources

                        if (!((preflightResult = (TeamListeningPreflightResult)v1) instanceof TeamListeningPreflightResult.Ready)) {
                            if (preflightResult instanceof TeamListeningPreflightResult.InvalidCode) {
                                TeamCompassViewModel.emitError$default(this.this$0, TeamCompassViewModel.access$tr(this.this$0, R.string.vm_error_team_code_invalid, new Object[0]), null, 2, null);
                                TeamCompassViewModel.logActionFailure$default(this.this$0, "startListening", this.$traceId, null, "invalid team code", ((TeamListeningPreflightResult.InvalidCode)preflightResult).getRawCode(), this.$uid, 4, null);
                                TeamCompassViewModel.access$stopListening(this.this$0);
                                return Unit.INSTANCE;
                            }
                            if (preflightResult instanceof TeamListeningPreflightResult.JoinFailure) {
                                if (TeamCompassViewModel.access$handleStartListeningTerminalFailure(this.this$0, ((TeamListeningPreflightResult.JoinFailure)preflightResult).getFailure(), ((TeamListeningPreflightResult.JoinFailure)preflightResult).getTeamCode(), this.$uid, this.$traceId)) {
                                    return Unit.INSTANCE;
                                }
                                Log.w((String)"TeamCompassVM", (String)("Membership ensure failed for team=" + this.$code), (Throwable)((TeamListeningPreflightResult.JoinFailure)preflightResult).getFailure().getCause());
                                TeamCompassViewModel.access$logActionFailure(this.this$0, "startListening", this.$traceId, ((TeamListeningPreflightResult.JoinFailure)preflightResult).getFailure().getCause(), ((TeamListeningPreflightResult.JoinFailure)preflightResult).getFailure().getMessage(), this.$code, this.$uid);
                            } else {
                                throw new NoWhenBranchMatchedException();
                            }
                        }
                        TeamCompassViewModel.access$startBackendHealthMonitor(this.this$0);
                        TeamCompassViewModel.access$startBackendStaleMonitor(this.this$0);
                        TeamCompassViewModel.access$startP2PInboundObservation(this.this$0, this.$code, this.$uid);
                        TeamCompassViewModel.access$logActionSuccess(this.this$0, "startListening", this.$traceId, this.$code, this.$uid);
                        this.label = 2;
                        v2 = TeamCompassViewModel.access$collectTeamSnapshotsWithReconnect(this.this$0, this.$code, this.$uid, (Continuation)this);
                        if (v2 == var6_2) {
                            return var6_2;
                        }
                        ** GOTO lbl47
                    }
                    case 2: {
                        ResultKt.throwOnFailure((Object)$result);
                        v2 = $result;
lbl47:
                        // 2 sources

                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        this.startMemberPrefsSync(code, uid);
    }

    private final boolean handleStartListeningTerminalFailure(TeamActionFailure failure, String teamCode, String uid, String traceId) {
        Pair pair;
        switch (WhenMappings.$EnumSwitchMapping$0[failure.getError().ordinal()]) {
            case 1: {
                Object[] objectArray = new Object[]{teamCode};
                pair = TuplesKt.to((Object)this.tr(R.string.vm_error_team_locked_format, objectArray), (Object)"team locked");
                break;
            }
            case 2: {
                Object[] objectArray = new Object[]{teamCode};
                pair = TuplesKt.to((Object)this.tr(R.string.vm_error_team_code_expired_format, objectArray), (Object)"team expired");
                break;
            }
            case 3: {
                Object[] objectArray = new Object[]{teamCode};
                pair = TuplesKt.to((Object)this.tr(R.string.vm_error_team_not_found_format, objectArray), (Object)"team not found");
                break;
            }
            case 4: {
                Object[] objectArray = new Object[]{teamCode};
                pair = TuplesKt.to((Object)this.tr(R.string.vm_error_team_permission_denied_format, objectArray), (Object)"permission denied");
                break;
            }
            default: {
                return false;
            }
        }
        Pair pair2 = pair;
        String userMessage = (String)pair2.component1();
        String logReason = (String)pair2.component2();
        TeamCompassViewModel.emitError$default(this, userMessage, null, 2, null);
        TeamCompassViewModel.logActionFailure$default(this, "startListening", traceId, null, logReason, teamCode, uid, 4, null);
        this.clearTeamSessionStateForTerminalFailure();
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).setTeamCode(null, (Continuation<? super Unit>)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
        return true;
    }

    private final void clearTeamSessionStateForTerminalFailure() {
        FilterUiState filterUiState;
        MapUiState mapUiState;
        TeamUiState teamUiState;
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
            teamUiState = TeamUiState.copy$default(it.getTeam(), null, null, CollectionsKt.emptyList(), CollectionsKt.emptyList(), null, 0L, null, null, false, 433, null);
            filterUiState = FilterUiState.copy$default(it.getFilter(), null, CollectionsKt.emptyList(), CollectionsKt.emptyList(), 1, null);
            mapUiState = MapUiState.copy$default(it.getMap(), CollectionsKt.emptyList(), CollectionsKt.emptyList(), CollectionsKt.emptyList(), CollectionsKt.emptyList(), false, null, null, false, 0.0f, 480, null);
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, teamUiState, mapUiState, filterUiState, null, null, null, 227, null))));
    }

    private final Object collectTeamSnapshotsWithReconnect(String teamCode, String uid, Continuation<? super Unit> $completion) {
        String backendDownMessage = this.tr(R.string.vm_error_backend_unavailable_retrying, new Object[0]);
        Object object = this.teamSnapshotObserver.collectWithReconnect(teamCode, uid, (Function0<? extends TeamViewMode>)((Function0)() -> TeamCompassViewModel.collectTeamSnapshotsWithReconnect$lambda$69(this)), (Function0<LocationPoint>)((Function0)() -> TeamCompassViewModel.collectTeamSnapshotsWithReconnect$lambda$70(this)), (Function2<? super TeamSnapshot, ? super Continuation<? super Unit>, ? extends Object>)((Function2)new Function2<TeamSnapshot, Continuation<? super Unit>, Object>(uid, this, teamCode, null){
            int label;
            /* synthetic */ Object L$0;
            final /* synthetic */ String $uid;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ String $teamCode;
            {
                this.$uid = $uid;
                this.this$0 = $receiver;
                this.$teamCode = $teamCode;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object object) {
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        MapUiState mapUiState;
                        TeamUiState teamUiState;
                        TrackingUiState trackingUiState;
                        UiState nextValue$iv;
                        Object prevValue$iv;
                        void $this$update$iv;
                        void $this$mapTo$iv$iv;
                        void $this$mapTo$iv$iv2;
                        UiState it;
                        Collection collection;
                        void $this$mapTo$iv$iv3;
                        ResultKt.throwOnFailure((Object)object);
                        TeamSnapshot snapshot = (TeamSnapshot)this.L$0;
                        long nowMs = System.currentTimeMillis();
                        Iterable $this$map$iv = snapshot.getTeamPoints();
                        boolean $i$f$map = false;
                        Iterable iterable = $this$map$iv;
                        Iterable<E> destination$iv$iv = new ArrayList<E>(CollectionsKt.collectionSizeOrDefault((Iterable)$this$map$iv, (int)10));
                        boolean $i$f$mapTo = false;
                        for (Object item$iv$iv : $this$mapTo$iv$iv3) {
                            TeamPoint teamPoint = (TeamPoint)item$iv$iv;
                            collection = destination$iv$iv;
                            boolean bl = false;
                            collection.add(TeamMarkerMappersKt.toUiMapPoint((TeamPoint)((Object)it)));
                        }
                        List mappedTeam = (List)destination$iv$iv;
                        Iterable $this$map$iv2 = snapshot.getPrivatePoints();
                        boolean $i$f$map2 = false;
                        destination$iv$iv = $this$map$iv2;
                        Iterable<E> destination$iv$iv2 = new ArrayList<E>(CollectionsKt.collectionSizeOrDefault((Iterable)$this$map$iv2, (int)10));
                        boolean $i$f$mapTo2 = false;
                        for (Object item$iv$iv : $this$mapTo$iv$iv2) {
                            void it2;
                            TeamPoint bl = (TeamPoint)item$iv$iv;
                            collection = destination$iv$iv2;
                            boolean bl2 = false;
                            collection.add(TeamMarkerMappersKt.toUiMapPoint((TeamPoint)it2));
                        }
                        List mappedPrivate = (List)destination$iv$iv2;
                        Iterable $this$map$iv3 = snapshot.getEnemyPings();
                        boolean $i$f$map222 = false;
                        destination$iv$iv2 = $this$map$iv3;
                        Collection destination$iv$iv3 = new ArrayList<E>(CollectionsKt.collectionSizeOrDefault((Iterable)$this$map$iv3, (int)10));
                        boolean $i$f$mapTo3 = false;
                        for (E item$iv$iv : $this$mapTo$iv$iv) {
                            void it3;
                            TeamEnemyPing bl2 = (TeamEnemyPing)item$iv$iv;
                            collection = destination$iv$iv3;
                            boolean bl = false;
                            collection.add(TeamMarkerMappersKt.toUiEnemyPing((TeamEnemyPing)it3));
                        }
                        List mappedEnemy = (List)destination$iv$iv3;
                        List<UnifiedMarker> mappedMarkers = TeamMarkerMappersKt.buildUnifiedMarkersForView(mappedTeam, mappedPrivate, mappedEnemy, this.$uid);
                        MutableStateFlow $i$f$map222 = TeamCompassViewModel.access$get_ui$p(this.this$0);
                        String string2 = this.$teamCode;
                        boolean $i$f$update = false;
                        do {
                            prevValue$iv = $this$update$iv.getValue();
                            it = (UiState)prevValue$iv;
                            boolean bl = false;
                            teamUiState = TeamUiState.copy$default(it.getTeam(), null, string2, snapshot.getPlayers(), snapshot.getRoleProfiles(), null, 0L, null, null, false, 433, null);
                            mapUiState = MapUiState.copy$default(it.getMap(), mappedTeam, mappedPrivate, mappedEnemy, mappedMarkers, false, null, null, false, 0.0f, 496, null);
                            trackingUiState = TrackingUiState.copy$default(it.getTracking(), false, false, false, null, null, null, false, false, TelemetryState.copy$default(it.getTracking().getTelemetry(), 0, 0, 0, 0L, null, true, 0L, nowMs, false, 0, 0, 1567, null), 255, null);
                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, trackingUiState, teamUiState, mapUiState, null, null, null, null, 113, null))));
                        TeamCompassViewModel.access$scheduleBackendStaleRefresh(this.this$0, nowMs);
                        TeamCompassViewModel.refreshTargetsFromState$default(this.this$0, 0L, 1, null);
                        TeamCompassViewModel.access$processEnemyPingAlerts(this.this$0, mappedEnemy);
                        TeamCompassViewModel.access$processSosAlerts(this.this$0);
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                var3_3.L$0 = value;
                return (Continuation)var3_3;
            }

            public final Object invoke(TeamSnapshot p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (Function3<? super Throwable, ? super Long, ? super Continuation<? super Unit>, ? extends Object>)((Function3)new Function3<Throwable, Long, Continuation<? super Unit>, Object>(this, backendDownMessage, null){
            int label;
            /* synthetic */ Object L$0;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ String $backendDownMessage;
            {
                this.this$0 = $receiver;
                this.$backendDownMessage = $backendDownMessage;
                super(3, $completion);
            }

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object object) {
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        TelemetryState previous;
                        long backendUnavailableSinceMs;
                        UiState it;
                        UiState nextValue$iv;
                        Object prevValue$iv;
                        void $this$update$iv;
                        ResultKt.throwOnFailure((Object)object);
                        Throwable err = (Throwable)this.L$0;
                        long nowMs = System.currentTimeMillis();
                        MutableStateFlow mutableStateFlow = TeamCompassViewModel.access$get_ui$p(this.this$0);
                        TeamCompassViewModel teamCompassViewModel = this.this$0;
                        boolean $i$f$update = false;
                        do {
                            prevValue$iv = $this$update$iv.getValue();
                            it = (UiState)prevValue$iv;
                            boolean bl = false;
                            previous = it.getTracking().getTelemetry();
                            Long l = Boxing.boxLong((long)previous.getBackendUnavailableSinceMs());
                            long value = ((Number)l).longValue();
                            boolean bl2 = false;
                            Long l2 = value > 0L ? l : null;
                            backendUnavailableSinceMs = l2 != null ? l2 : nowMs;
                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, TrackingUiState.copy$default(it.getTracking(), false, false, false, null, null, null, false, false, TelemetryState.copy$default(it.getTracking().getTelemetry(), it.getTracking().getTelemetry().getRtdbReadErrors() + 1, 0, 0, 0L, null, false, backendUnavailableSinceMs, 0L, TeamCompassViewModel.access$computeBackendStale(teamCompassViewModel, previous.getLastSnapshotAtMs(), nowMs), 0, 0, 1694, null), 255, null), null, null, null, null, null, null, 253, null))));
                        TeamCompassViewModel.access$scheduleBackendStaleRefresh(this.this$0, nowMs);
                        if (!Intrinsics.areEqual((Object)((UiState)TeamCompassViewModel.access$get_ui$p(this.this$0).getValue()).getLastError(), (Object)this.$backendDownMessage)) {
                            this.this$0.emitError(this.$backendDownMessage, err);
                        }
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(Throwable p1, long p2, Continuation<? super Unit> p3) {
                var var5_4 = new /* invalid duplicate definition of identical inner class */;
                var5_4.L$0 = p1;
                return var5_4.invokeSuspend(Unit.INSTANCE);
            }
        }), $completion);
        if (object == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
            return object;
        }
        return Unit.INSTANCE;
    }

    private final void stopListening() {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        Job job = this.teamObserverJob;
        if (job != null) {
            Job.DefaultImpls.cancel$default((Job)job, null, (int)1, null);
        }
        this.teamObserverJob = null;
        Job job2 = this.p2pObserverJob;
        if (job2 != null) {
            Job.DefaultImpls.cancel$default((Job)job2, null, (int)1, null);
        }
        this.p2pObserverJob = null;
        this.backendHealthDelegate.stop();
        this.lastBackendHealthAvailableSample = true;
        Job job3 = this.memberPrefsObserverJob;
        if (job3 != null) {
            Job.DefaultImpls.cancel$default((Job)job3, null, (int)1, null);
        }
        this.memberPrefsObserverJob = null;
        Job job4 = this.memberPrefsSyncJob;
        if (job4 != null) {
            Job.DefaultImpls.cancel$default((Job)job4, null, (int)1, null);
        }
        this.memberPrefsSyncJob = null;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, TrackingUiState.copy$default(it.getTracking(), false, false, false, null, null, null, false, false, TelemetryState.copy$default(it.getTracking().getTelemetry(), 0, 0, 0, 0L, null, true, 0L, 0L, false, 0, 0, 1567, null), 255, null), null, null, null, null, null, null, 253, null))));
    }

    private final void startP2PInboundObservation(String teamCode, String localUid) {
        Job job = this.p2pObserverJob;
        if (job != null) {
            Job.DefaultImpls.cancel$default((Job)job, null, (int)1, null);
        }
        this.p2pObserverJob = BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, teamCode, localUid, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ String $teamCode;
            final /* synthetic */ String $localUid;
            {
                this.this$0 = $receiver;
                this.$teamCode = $teamCode;
                this.$localUid = $localUid;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = FlowKt.collectLatest(TeamCompassViewModel.access$getTeamSessionDelegate$p(this.this$0).observeP2PInbound(this.$teamCode), (Function2)((Function2)new Function2<P2PInboundMessage, Continuation<? super Unit>, Object>(this.this$0, this.$localUid, null){
                            int label;
                            /* synthetic */ Object L$0;
                            final /* synthetic */ TeamCompassViewModel this$0;
                            final /* synthetic */ String $localUid;
                            {
                                this.this$0 = $receiver;
                                this.$localUid = $localUid;
                                super(2, $completion);
                            }

                            public final Object invokeSuspend(Object object) {
                                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch (this.label) {
                                    case 0: {
                                        ResultKt.throwOnFailure((Object)object);
                                        P2PInboundMessage inbound = (P2PInboundMessage)this.L$0;
                                        TeamCompassViewModel.access$handleP2PInbound(this.this$0, inbound, this.$localUid);
                                        return Unit.INSTANCE;
                                    }
                                }
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }

                            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                                var var3_3 = new /* invalid duplicate definition of identical inner class */;
                                var3_3.L$0 = value;
                                return (Continuation)var3_3;
                            }

                            public final Object invoke(P2PInboundMessage p1, Continuation<? super Unit> p2) {
                                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                            }
                        }), (Continuation)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        Object object3;
                        try {
                            void $result;
                            ResultKt.throwOnFailure((Object)$result);
                            object3 = $result;
                            return Unit.INSTANCE;
                        }
                        catch (CancellationException cancel) {
                            throw cancel;
                        }
                        catch (Throwable err) {
                            UiState state;
                            UiState nextValue$iv;
                            Object prevValue$iv;
                            MutableStateFlow $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                            boolean $i$f$update = false;
                            do {
                                prevValue$iv = $this$update$iv.getValue();
                                state = (UiState)prevValue$iv;
                                boolean bl = false;
                            } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(state, null, TrackingUiState.copy$default(state.getTracking(), false, false, false, null, null, null, false, false, TelemetryState.copy$default(state.getTelemetry(), 0, 0, 0, 0L, null, false, 0L, 0L, false, 0, state.getTelemetry().getP2pInboundErrors() + 1, 1023, null), 255, null), null, null, null, null, null, null, 253, null))));
                            TeamCompassViewModel.access$logActionFailure(this.this$0, "p2pInboundStream", TeamCompassViewModel.access$newTraceId(this.this$0, "p2pInboundStream"), err, "p2p inbound stream failed", this.$teamCode, this.$localUid);
                        }
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    private final void handleP2PInbound(P2PInboundMessage inbound, String localUid) {
        UiState state;
        UiState nextValue$iv;
        Object prevValue$iv;
        String senderId = inbound.getMessage().getMetadata().getSenderId();
        if (Intrinsics.areEqual((Object)senderId, (Object)localUid)) {
            return;
        }
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            state = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(state, null, TrackingUiState.copy$default(state.getTracking(), false, false, false, null, null, null, false, false, TelemetryState.copy$default(state.getTelemetry(), 0, 0, 0, 0L, null, false, 0L, 0L, false, state.getTelemetry().getP2pInboundMessages() + 1, 0, 1535, null), 255, null), null, null, null, null, null, null, 253, null))));
        Log.i((String)TAG, (String)("P2P inbound via " + inbound.getTransportName() + ": type=" + inbound.getMessage().getMetadata().getType() + " sender=" + senderId));
    }

    private final void startBackendHealthMonitor() {
        String backendDownMessage = this.tr(R.string.vm_error_backend_unavailable_retrying, new Object[0]);
        this.backendHealthDelegate.startHealthMonitor(ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (Function4<? super Boolean, ? super Boolean, ? super Long, ? super Continuation<? super Unit>, ? extends Object>)((Function4)new Function4<Boolean, Boolean, Long, Continuation<? super Unit>, Object>(this, backendDownMessage, null){
            int label;
            /* synthetic */ boolean Z$0;
            /* synthetic */ long J$0;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ String $backendDownMessage;
            {
                this.this$0 = $receiver;
                this.$backendDownMessage = $backendDownMessage;
                super(4, $completion);
            }

            public final Object invokeSuspend(Object object) {
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        UiState state;
                        boolean bl;
                        long l;
                        boolean bl2;
                        String string2;
                        long l2;
                        int n;
                        int n2;
                        int n3;
                        TelemetryState telemetryState;
                        boolean bl3;
                        boolean bl4;
                        TrackingMode trackingMode;
                        Double d;
                        LocationPoint locationPoint;
                        boolean bl5;
                        boolean bl6;
                        boolean bl7;
                        TrackingUiState trackingUiState;
                        AuthState authState;
                        UiState uiState;
                        UiState nextValue$iv;
                        Object prevValue$iv;
                        MutableStateFlow $this$update$iv;
                        ResultKt.throwOnFailure((Object)object);
                        boolean available = this.Z$0;
                        long nowMs = this.J$0;
                        TeamCompassViewModel.access$setLastBackendHealthAvailableSample$p(this.this$0, available);
                        Ref.BooleanRef becameUnavailable = new Ref.BooleanRef();
                        Ref.BooleanRef becameRecovered = new Ref.BooleanRef();
                        MutableStateFlow mutableStateFlow = TeamCompassViewModel.access$get_ui$p(this.this$0);
                        Object object2 = this.this$0;
                        boolean $i$f$update = false;
                        do {
                            prevValue$iv = $this$update$iv.getValue();
                            state = (UiState)prevValue$iv;
                            boolean bl8 = false;
                            TelemetryState telemetry = state.getTracking().getTelemetry();
                            boolean stale = TeamCompassViewModel.access$computeBackendStale(object2, telemetry.getLastSnapshotAtMs(), nowMs);
                            boolean hasFreshSnapshot = telemetry.getLastSnapshotAtMs() > 0L && !stale;
                            boolean effectiveAvailable = available || hasFreshSnapshot;
                            becameUnavailable.element = telemetry.getBackendAvailable() && !effectiveAvailable;
                            becameRecovered.element = !telemetry.getBackendAvailable() && effectiveAvailable;
                            uiState = state;
                            authState = null;
                            trackingUiState = state.getTracking();
                            bl7 = false;
                            bl6 = false;
                            bl5 = false;
                            locationPoint = null;
                            d = null;
                            trackingMode = null;
                            bl4 = false;
                            bl3 = false;
                            telemetryState = telemetry;
                            n3 = 0;
                            n2 = 0;
                            n = 0;
                            l2 = 0L;
                            string2 = null;
                            bl2 = effectiveAvailable;
                            if (effectiveAvailable) {
                                l = 0L;
                            } else {
                                Long l3 = Boxing.boxLong((long)telemetry.getBackendUnavailableSinceMs());
                                long it = ((Number)l3).longValue();
                                boolean bl9 = bl2;
                                String string3 = string2;
                                long l4 = l2;
                                int n4 = n;
                                int n5 = n2;
                                int n6 = n3;
                                TelemetryState telemetryState2 = telemetryState;
                                TrackingMode trackingMode2 = trackingMode;
                                Double d2 = d;
                                LocationPoint locationPoint2 = locationPoint;
                                TrackingUiState trackingUiState2 = trackingUiState;
                                AuthState authState2 = authState;
                                UiState uiState2 = uiState;
                                boolean bl10 = false;
                                boolean bl11 = it > 0L;
                                uiState = uiState2;
                                authState = authState2;
                                trackingUiState = trackingUiState2;
                                bl7 = false;
                                bl6 = false;
                                bl5 = false;
                                locationPoint = locationPoint2;
                                d = d2;
                                trackingMode = trackingMode2;
                                bl4 = false;
                                bl3 = false;
                                telemetryState = telemetryState2;
                                n3 = n6;
                                n2 = n5;
                                n = n4;
                                l2 = l4;
                                string2 = string3;
                                bl2 = bl9;
                                Long l5 = bl11 ? l3 : null;
                                l = l5 != null ? l5 : nowMs;
                            }
                            if (stale) {
                                bl = true;
                                continue;
                            }
                            bl = false;
                        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(uiState, authState, TrackingUiState.copy$default(trackingUiState, bl7, bl6, bl5, locationPoint, d, trackingMode, bl4, bl3, TelemetryState.copy$default(telemetryState, n3, n2, n, l2, string2, bl2, l, 0L, bl, 0, 0, 1695, null), 255, null), null, null, null, null, null, null, 253, null))));
                        TeamCompassViewModel.access$scheduleBackendStaleRefresh(this.this$0, nowMs);
                        if (becameUnavailable.element) {
                            TeamCompassViewModel.emitError$default(this.this$0, this.$backendDownMessage, null, 2, null);
                        } else if (becameRecovered.element) {
                            UiState uiState3;
                            UiState nextValue$iv2;
                            $this$update$iv = TeamCompassViewModel.access$get_ui$p(this.this$0);
                            object2 = this.$backendDownMessage;
                            $i$f$update = false;
                            do {
                                prevValue$iv = $this$update$iv.getValue();
                                state = (UiState)prevValue$iv;
                                boolean bl12 = false;
                                if (Intrinsics.areEqual((Object)state.getLastError(), (Object)object2)) {
                                    uiState3 = UiState.copy$default(state, null, null, null, null, null, null, null, null, 127, null);
                                    continue;
                                }
                                uiState3 = state;
                            } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv2 = uiState3)));
                        }
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(boolean p1, Boolean p2, long p3, Continuation<? super Unit> p4) {
                var var6_5 = new /* invalid duplicate definition of identical inner class */;
                var6_5.Z$0 = p1;
                var6_5.J$0 = p3;
                return var6_5.invokeSuspend(Unit.INSTANCE);
            }
        }));
    }

    private final void startBackendStaleMonitor() {
        TeamCompassViewModel.scheduleBackendStaleRefresh$default(this, 0L, 1, null);
    }

    private final void scheduleBackendStaleRefresh(long nowMs) {
        this.backendHealthDelegate.scheduleStaleRefresh(ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), nowMs, (Function0<Long>)((Function0)() -> TeamCompassViewModel.scheduleBackendStaleRefresh$lambda$73(this)), (Function1<? super Long, Unit>)((Function1)arg_0 -> TeamCompassViewModel.scheduleBackendStaleRefresh$lambda$74(this, arg_0)));
    }

    static /* synthetic */ void scheduleBackendStaleRefresh$default(TeamCompassViewModel teamCompassViewModel, long l, int n, Object object) {
        if ((n & 1) != 0) {
            l = System.currentTimeMillis();
        }
        teamCompassViewModel.scheduleBackendStaleRefresh(l);
    }

    private final void refreshBackendStaleFlag(long nowMs) {
        UiState state;
        UiState uiState;
        UiState nextValue$iv;
        Object prevValue$iv;
        String backendDownMessage = this.tr(R.string.vm_error_backend_unavailable_retrying, new Object[0]);
        boolean becameUnavailable = false;
        boolean becameRecovered = false;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            boolean changed;
            long l;
            boolean effectiveAvailable;
            prevValue$iv = $this$update$iv.getValue();
            state = (UiState)prevValue$iv;
            boolean bl = false;
            TelemetryState telemetry = state.getTracking().getTelemetry();
            boolean stale = this.computeBackendStale(telemetry.getLastSnapshotAtMs(), nowMs);
            boolean hasFreshSnapshot = telemetry.getLastSnapshotAtMs() > 0L && !stale;
            boolean bl2 = effectiveAvailable = this.lastBackendHealthAvailableSample || hasFreshSnapshot;
            if (effectiveAvailable) {
                l = 0L;
            } else {
                Long l2 = telemetry.getBackendUnavailableSinceMs();
                long it = ((Number)l2).longValue();
                boolean bl3 = false;
                Long l3 = it > 0L ? l2 : null;
                l = l3 != null ? l3 : nowMs;
            }
            long unavailableSinceMs = l;
            boolean bl4 = changed = stale != telemetry.isBackendStale() || effectiveAvailable != telemetry.getBackendAvailable() || unavailableSinceMs != telemetry.getBackendUnavailableSinceMs();
            if (!changed) {
                uiState = state;
                continue;
            }
            becameUnavailable = telemetry.getBackendAvailable() && !effectiveAvailable;
            becameRecovered = !telemetry.getBackendAvailable() && effectiveAvailable;
            uiState = UiState.copy$default(state, null, TrackingUiState.copy$default(state.getTracking(), false, false, false, null, null, null, false, false, TelemetryState.copy$default(telemetry, 0, 0, 0, 0L, null, effectiveAvailable, unavailableSinceMs, 0L, stale, 0, 0, 1695, null), 255, null), null, null, null, null, null, null, 253, null);
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = uiState)));
        if (becameUnavailable) {
            TeamCompassViewModel.emitError$default(this, backendDownMessage, null, 2, null);
        } else if (becameRecovered) {
            UiState uiState2;
            UiState nextValue$iv2;
            $this$update$iv = this._ui;
            $i$f$update = false;
            do {
                prevValue$iv = $this$update$iv.getValue();
                state = (UiState)prevValue$iv;
                boolean bl = false;
                if (Intrinsics.areEqual((Object)state.getLastError(), (Object)backendDownMessage)) {
                    uiState2 = UiState.copy$default(state, null, null, null, null, null, null, null, null, 127, null);
                    continue;
                }
                uiState2 = state;
            } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv2 = uiState2)));
        }
    }

    static /* synthetic */ void refreshBackendStaleFlag$default(TeamCompassViewModel teamCompassViewModel, long l, int n, Object object) {
        if ((n & 1) != 0) {
            l = System.currentTimeMillis();
        }
        teamCompassViewModel.refreshBackendStaleFlag(l);
    }

    private final boolean computeBackendStale(long lastSnapshotAtMs, long nowMs) {
        return this.backendHealthDelegate.computeBackendStale(lastSnapshotAtMs, nowMs);
    }

    private final void startMemberPrefsSync(String teamCode, String uid) {
        Job job = this.memberPrefsObserverJob;
        if (job != null) {
            Job.DefaultImpls.cancel$default((Job)job, null, (int)1, null);
        }
        this.memberPrefsObserverJob = null;
        Job job2 = this.memberPrefsSyncJob;
        if (job2 != null) {
            Job.DefaultImpls.cancel$default((Job)job2, null, (int)1, null);
        }
        this.memberPrefsSyncJob = null;
        Flow $this$map$iv = (Flow)this.ui;
        boolean $i$f$map = false;
        Flow $this$unsafeTransform$iv$iv = $this$map$iv;
        boolean $i$f$unsafeTransform = false;
        boolean $i$f$unsafeFlow = false;
        MemberPrefsSyncJobs jobs = this.memberPrefsSyncWorker.start(ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), teamCode, uid, (Flow<TargetFilterState>)((Flow)new Flow<TargetFilterState>($this$unsafeTransform$iv$iv){
            final /* synthetic */ Flow $this_unsafeTransform$inlined;
            {
                this.$this_unsafeTransform$inlined = flow;
            }

            public Object collect(FlowCollector collector, Continuation $completion) {
                Continuation continuation = $completion;
                FlowCollector $this$unsafeTransform_u24lambda_u240 = collector;
                boolean bl = false;
                Object object = this.$this_unsafeTransform$inlined.collect(new FlowCollector($this$unsafeTransform_u24lambda_u240){
                    final /* synthetic */ FlowCollector $this_unsafeFlow;
                    {
                        this.$this_unsafeFlow = $receiver;
                    }

                    /*
                     * Unable to fully structure code
                     */
                    public final Object emit(Object var1_1, Continuation $completion) {
                        if (!($completion instanceof startMemberPrefsSync$$inlined$map$1$2$1)) ** GOTO lbl-1000
                        var3_3 = $completion;
                        if ((var3_3.label & -2147483648) != 0) {
                            var3_3.label -= -2147483648;
                        } else lbl-1000:
                        // 2 sources

                        {
                            $continuation = new ContinuationImpl(this, $completion){
                                /* synthetic */ Object result;
                                int label;
                                Object L$0;
                                final /* synthetic */ startMemberPrefsSync$$inlined$map$1$2 this$0;
                                {
                                    this.this$0 = this$0;
                                    super($completion);
                                }

                                public final Object invokeSuspend(Object $result) {
                                    this.result = $result;
                                    this.label |= Integer.MIN_VALUE;
                                    return this.this$0.emit(null, (Continuation)this);
                                }
                            };
                        }
                        $result = $continuation.result;
                        var5_5 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                        switch ($continuation.label) {
                            case 0: {
                                ResultKt.throwOnFailure((Object)$result);
                                var6_6 = value;
                                $this$map_u24lambda_u245 = this.$this_unsafeFlow;
                                $i$a$-unsafeTransform-FlowKt__TransformKt$map$1 = false;
                                var9_10 = $this$map_u24lambda_u245;
                                (Continuation)$continuation;
                                it = (UiState)value;
                                $i$a$-map-TeamCompassViewModel$startMemberPrefsSync$jobs$1 = false;
                                $continuation.label = 1;
                                v0 = var9_10.emit((Object)it.getTargetFilterState(), (Continuation)$continuation);
                                if (v0 == var5_5) {
                                    return var5_5;
                                }
                                ** GOTO lbl29
                            }
                            case 1: {
                                $i$a$-unsafeTransform-FlowKt__TransformKt$map$1 = false;
                                ResultKt.throwOnFailure((Object)$result);
                                v0 = $result;
lbl29:
                                // 2 sources

                                return Unit.INSTANCE;
                            }
                        }
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                }, $completion);
                if (object == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
                    return object;
                }
                return Unit.INSTANCE;
            }
        }), (Function1<? super TargetFilterState, TeamMemberPrefs>)((Function1)arg_0 -> TeamCompassViewModel.startMemberPrefsSync$lambda$79(this, arg_0)), (Function1<? super TeamMemberPrefs, Unit>)((Function1)arg_0 -> TeamCompassViewModel.startMemberPrefsSync$lambda$81(this, arg_0)), (Function0<Boolean>)((Function0)() -> TeamCompassViewModel.startMemberPrefsSync$lambda$82(this)), (Function0<Unit>)((Function0)() -> TeamCompassViewModel.startMemberPrefsSync$lambda$83(this)), (Function2<? super TeamActionFailure, ? super Boolean, Unit>)((Function2)(arg_0, arg_1) -> TeamCompassViewModel.startMemberPrefsSync$lambda$84(this, arg_0, arg_1)));
        this.memberPrefsObserverJob = jobs.getObserverJob();
        this.memberPrefsSyncJob = jobs.getSyncJob();
    }

    /*
     * WARNING - void declaration
     */
    private final void processEnemyPingAlerts(List<EnemyPing> enemyPings) {
        void $this$filterNotTo$iv$iv;
        Iterable $this$filterNot$iv = enemyPings;
        boolean $i$f$filterNot = false;
        Iterable iterable = $this$filterNot$iv;
        Collection destination$iv$iv = new ArrayList();
        boolean $i$f$filterNotTo = false;
        for (Object element$iv$iv : $this$filterNotTo$iv$iv) {
            EnemyPing it = (EnemyPing)element$iv$iv;
            boolean bl = false;
            if (it.isBluetooth()) continue;
            destination$iv$iv.add(element$iv$iv);
        }
        List tacticalEnemyPings = (List)destination$iv$iv;
        long now = System.currentTimeMillis();
        int closeAlerts = this.alertsCoordinator.consumeNewCloseEnemyPings(tacticalEnemyPings, ((UiState)this._ui.getValue()).getMe(), now);
        int n = 0;
        while (n < closeAlerts) {
            int it = n++;
            boolean bl = false;
            this.vibrateAndBeep(true);
        }
    }

    private final void processSosAlerts() {
        long now = System.currentTimeMillis();
        List<PlayerState> players = ((UiState)this._ui.getValue()).getPlayers();
        Iterable $this$forEach$iv = players;
        boolean $i$f$forEach = false;
        for (Object element$iv : $this$forEach$iv) {
            PlayerState player = (PlayerState)element$iv;
            boolean bl = false;
            if (player.getSosUntilMs() <= now) continue;
            this.eventNotificationManager.showSosAlert(player);
        }
    }

    private final void vibrateAndBeep(boolean strong) {
        boolean hasVibratePermission;
        boolean bl = hasVibratePermission = ContextCompat.checkSelfPermission((Context)((Context)this.application), (String)"android.permission.VIBRATE") == 0;
        if (hasVibratePermission) {
            try {
                if (strong) {
                    long[] lArray = new long[]{0L, 90L, 60L, 120L, 60L, 220L};
                    long[] timings = lArray;
                    Vibrator vibrator = this.vibrator;
                    if (vibrator != null) {
                        vibrator.vibrate(VibrationEffect.createWaveform((long[])timings, (int)-1));
                    }
                } else {
                    Vibrator vibrator = this.vibrator;
                    if (vibrator != null) {
                        vibrator.vibrate(VibrationEffect.createOneShot((long)200L, (int)-1));
                    }
                }
            }
            catch (Throwable err) {
                Log.w((String)TAG, (String)"Failed to vibrate alert", (Throwable)err);
            }
        }
        try {
            int toneType = strong ? 21 : 24;
            int durationMs = strong ? 450 : 160;
            this.tone.startTone(toneType, durationMs);
        }
        catch (Throwable err) {
            Log.w((String)TAG, (String)"Failed to play alert tone", (Throwable)err);
        }
    }

    private final void startDeadReminder() {
        if (this.deadReminderJob != null) {
            return;
        }
        this.deadReminderJob = BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object var1_1) {
                var2_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)var1_1);
lbl6:
                        // 3 sources

                        while (true) {
                            this.label = 1;
                            v0 = DelayKt.delay((long)600000L, (Continuation)((Continuation)this));
                            if (v0 == var2_2) {
                                return var2_2;
                            }
                            ** GOTO lbl15
                            break;
                        }
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        v0 = $result;
lbl15:
                        // 2 sources

                        if (((UiState)TeamCompassViewModel.access$get_ui$p(this.this$0).getValue()).getPlayerMode() != PlayerMode.DEAD) ** GOTO lbl6
                        TeamCompassViewModel.access$vibrateAndBeep(this.this$0, true);
                        ** continue;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    private final void stopDeadReminder() {
        Job job = this.deadReminderJob;
        if (job != null) {
            Job.DefaultImpls.cancel$default((Job)job, null, (int)1, null);
        }
        this.deadReminderJob = null;
    }

    /*
     * WARNING - void declaration
     */
    public final void startTracking(@NotNull TrackingMode mode, boolean persistMode) {
        void $this$startTracking_u24lambda_u2490;
        UiState state;
        UiState nextValue$iv;
        Object prevValue$iv;
        Intrinsics.checkNotNullParameter((Object)mode, (String)"mode");
        if (!this.locationReadinessCoordinator.hasLocationPermission()) {
            UiState it;
            UiState nextValue$iv2;
            Object prevValue$iv2;
            TeamCompassViewModel.emitError$default(this, this.tr(R.string.vm_error_location_permission_required, new Object[0]), null, 2, null);
            MutableStateFlow<UiState> $this$update$iv = this._ui;
            boolean $i$f$update = false;
            do {
                prevValue$iv2 = $this$update$iv.getValue();
                it = (UiState)prevValue$iv2;
                boolean bl = false;
            } while (!$this$update$iv.compareAndSet(prevValue$iv2, (Object)(nextValue$iv2 = UiState.copy$default(it, null, TrackingUiState.copy$default(it.getTracking(), false, false, false, null, null, null, false, false, null, 509, null), null, null, null, null, null, null, 253, null))));
            return;
        }
        Bundle $this$update$iv = this._ui;
        boolean $i$f$update22 = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            state = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = this.locationReadinessCoordinator.applyServiceState(state))));
        if (!((UiState)this._ui.getValue()).isLocationServiceEnabled()) {
            TeamCompassViewModel.emitError$default(this, this.tr(R.string.vm_error_location_services_disabled, new Object[0]), null, 2, null);
            return;
        }
        Bundle $i$f$update22 = $this$update$iv = new Bundle();
        String string2 = "start_tracking";
        FirebaseAnalytics firebaseAnalytics = this.analytics;
        boolean bl = false;
        $this$startTracking_u24lambda_u2490.putString("mode", mode.name());
        $this$startTracking_u24lambda_u2490.putString("team_code", ((UiState)this._ui.getValue()).getTeamCode());
        Unit unit = Unit.INSTANCE;
        firebaseAnalytics.logEvent(string2, $this$update$iv);
        UiState state2 = (UiState)this._ui.getValue();
        String string3 = state2.getTeamCode();
        if (string3 == null) {
            return;
        }
        String code = string3;
        String string4 = state2.getUid();
        if (string4 == null) {
            return;
        }
        String uid = string4;
        if (persistMode) {
            UiState it;
            UiState nextValue$iv3;
            Object prevValue$iv3;
            MutableStateFlow<UiState> $this$update$iv2 = this._ui;
            boolean $i$f$update = false;
            do {
                prevValue$iv3 = $this$update$iv2.getValue();
                it = (UiState)prevValue$iv3;
                boolean bl2 = false;
            } while (!$this$update$iv2.compareAndSet(prevValue$iv3, (Object)(nextValue$iv3 = UiState.copy$default(it, null, TrackingUiState.copy$default(it.getTracking(), false, false, false, null, null, mode, false, false, null, 479, null), null, null, null, null, null, null, 253, null))));
            BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, mode, null){
                int label;
                final /* synthetic */ TeamCompassViewModel this$0;
                final /* synthetic */ TrackingMode $mode;
                {
                    this.this$0 = $receiver;
                    this.$mode = $mode;
                    super(2, $completion);
                }

                /*
                 * WARNING - void declaration
                 * Enabled force condition propagation
                 * Lifted jumps to return sites
                 */
                public final Object invokeSuspend(Object object) {
                    Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                    switch (this.label) {
                        case 0: {
                            ResultKt.throwOnFailure((Object)object);
                            this.label = 1;
                            Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).setDefaultMode(this.$mode, (Continuation<? super Unit>)((Continuation)this));
                            if (object3 != object2) return Unit.INSTANCE;
                            return object2;
                        }
                        case 1: {
                            void $result;
                            ResultKt.throwOnFailure((Object)$result);
                            Object object3 = $result;
                            return Unit.INSTANCE;
                        }
                    }
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }

                public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                    return (Continuation)new /* invalid duplicate definition of identical inner class */;
                }

                public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                    return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
                }
            }), (int)2, null);
        }
        this.startHeading();
        TrackingCoordinator.start$default(this.trackingCoordinator, new TrackingCoordinator.StartRequest(code, uid, state2.getCallsign(), mode, state2.getGameIntervalSec(), state2.getGameDistanceM(), state2.getSilentIntervalSec(), state2.getSilentDistanceM(), state2.getPlayerMode(), state2.getMySosUntilMs()), state2.isTracking(), false, 4, null);
        if (state2.getPlayerMode() == PlayerMode.DEAD) {
            this.startDeadReminder();
        } else {
            this.stopDeadReminder();
        }
    }

    public static /* synthetic */ void startTracking$default(TeamCompassViewModel teamCompassViewModel, TrackingMode trackingMode, boolean bl, int n, Object object) {
        if ((n & 2) != 0) {
            bl = true;
        }
        teamCompassViewModel.startTracking(trackingMode, bl);
    }

    private final void restartTracking() {
        if (!((UiState)this._ui.getValue()).isTracking()) {
            return;
        }
        this.startTracking(((UiState)this._ui.getValue()).getDefaultMode(), false);
    }

    public final void stopTracking() {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        this.trackingCoordinator.stop();
        this.stopHeading();
        this.stopDeadReminder();
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, TrackingUiState.copy$default(it.getTracking(), false, false, false, null, null, null, false, false, null, 382, null), null, null, null, null, null, null, 253, null))));
    }

    public final void dismissError() {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, null, null, null, null, 127, null))));
    }

    @NotNull
    public final List<CompassTarget> computeTargets(long nowMs) {
        UiState state = (UiState)this._ui.getValue();
        Pair<List<PrioritizedTarget>, List<CompassTarget>> pair = this.targetFilterCoordinator.buildTargetsForState(state, nowMs);
        List prioritized = (List)pair.component1();
        List display = (List)pair.component2();
        if (!Intrinsics.areEqual(state.getPrioritizedTargets(), (Object)prioritized) || !Intrinsics.areEqual(state.getDisplayTargets(), (Object)display)) {
            UiState it;
            UiState nextValue$iv;
            Object prevValue$iv;
            MutableStateFlow<UiState> $this$update$iv = this._ui;
            boolean $i$f$update = false;
            do {
                prevValue$iv = $this$update$iv.getValue();
                it = (UiState)prevValue$iv;
                boolean bl = false;
            } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, FilterUiState.copy$default(it.getFilter(), null, prioritized, display, 1, null), null, null, null, 239, null))));
        }
        return display;
    }

    private final void refreshTargetsFromState(long nowMs) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        UiState state = (UiState)this._ui.getValue();
        Pair<List<PrioritizedTarget>, List<CompassTarget>> pair = this.targetFilterCoordinator.buildTargetsForState(state, nowMs);
        List prioritized = (List)pair.component1();
        List display = (List)pair.component2();
        if (Intrinsics.areEqual(state.getPrioritizedTargets(), (Object)prioritized) && Intrinsics.areEqual(state.getDisplayTargets(), (Object)display)) {
            return;
        }
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, null, null, null, FilterUiState.copy$default(it.getFilter(), null, prioritized, display, 1, null), null, null, null, 239, null))));
    }

    static /* synthetic */ void refreshTargetsFromState$default(TeamCompassViewModel teamCompassViewModel, long l, int n, Object object) {
        if ((n & 1) != 0) {
            l = System.currentTimeMillis();
        }
        teamCompassViewModel.refreshTargetsFromState(l);
    }

    private final void startHeading() {
        this.getHeadingSensorCoordinator().start();
    }

    private final void stopHeading() {
        this.getHeadingSensorCoordinator().stop();
    }

    private final void startLocationServiceMonitor() {
        Job job = this.locationServiceMonitorJob;
        if (job != null) {
            Job.DefaultImpls.cancel$default((Job)job, null, (int)1, null);
        }
        this.locationServiceMonitorJob = this.locationReadinessCoordinator.startLocationServiceMonitor(ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (Function0<UiState>)((Function0)() -> TeamCompassViewModel.startLocationServiceMonitor$lambda$96(this)), (Function1<? super Function1<? super UiState, UiState>, Unit>)((Function1)arg_0 -> TeamCompassViewModel.startLocationServiceMonitor$lambda$97(this, arg_0)), (Function1<? super String, Unit>)((Function1)arg_0 -> TeamCompassViewModel.startLocationServiceMonitor$lambda$98(this, arg_0)), this.tr(R.string.vm_error_location_services_disabled, new Object[0]), this.tr(R.string.vm_error_location_disabled_during_tracking, new Object[0]), (Function1<? super Boolean, Long>)((Function1)new Function1<Boolean, Long>((Object)Companion){

            public final Long invoke(boolean p0) {
                return ((Companion)this.receiver).locationServicePollIntervalMs$app_debug(p0);
            }
        }));
    }

    private final String normalizeTeamCode(String raw) {
        return this.teamSessionDelegate.normalizeTeamCode(raw);
    }

    private final String tr(@StringRes int resId, Object ... args) {
        String string2 = this.application.getString(resId, Arrays.copyOf(args, args.length));
        Intrinsics.checkNotNullExpressionValue((Object)string2, (String)"getString(...)");
        return string2;
    }

    private final void handleActionFailure(String defaultMessage, TeamActionFailure failure) {
        String message = TeamActionErrorPolicy.INSTANCE.toUserMessage((Context)this.getApplication(), defaultMessage, failure);
        this.emitError(message, failure.getCause());
    }

    public final void logPerfMetricsSnapshot() {
        TeamCompassPerfSnapshot snapshot = TeamCompassPerfMetrics.INSTANCE.snapshot();
        long l = snapshot.getRtdbSnapshotEmits();
        long l2 = snapshot.getRtdbCleanupSweeps();
        long l3 = snapshot.getRtdbCleanupWrites();
        long l4 = snapshot.getMapBitmapLoadRequests();
        long l5 = snapshot.getMapBitmapCacheHits();
        long l6 = snapshot.getMapBitmapDecodes();
        String string2 = "%.1f";
        Object[] objectArray = new Object[]{snapshot.getAverageMapBitmapDecodeMs()};
        String string3 = String.format(string2, Arrays.copyOf(objectArray, objectArray.length));
        Intrinsics.checkNotNullExpressionValue((Object)string3, (String)"format(...)");
        long l7 = snapshot.getFullscreenMapFirstRenderSamples();
        string2 = "%.1f";
        objectArray = new Object[]{snapshot.getAverageFullscreenMapFirstRenderMs()};
        String string4 = String.format(string2, Arrays.copyOf(objectArray, objectArray.length));
        Intrinsics.checkNotNullExpressionValue((Object)string4, (String)"format(...)");
        Log.i((String)TAG, (String)("perf snapshot: rtdbEmits=" + l + ", cleanupSweeps=" + l2 + ", cleanupWrites=" + l3 + ", mapLoads=" + l4 + ", mapHits=" + l5 + ", mapDecodes=" + l6 + ", mapAvgDecodeMs=" + string3 + ", firstRenderSamples=" + l7 + ", firstRenderAvgMs=" + string4 + ", peakUsedMemBytes=" + snapshot.getPeakAppUsedMemoryBytes()));
    }

    protected void onCleared() {
        this.stopHeading();
        this.stopListening();
        this.stopDeadReminder();
        Job job = this.locationServiceMonitorJob;
        if (job != null) {
            Job.DefaultImpls.cancel$default((Job)job, null, (int)1, null);
        }
        this.locationServiceMonitorJob = null;
        this.autoBrightnessBinding.clear();
        if (this.bluetoothScanCoordinatorLazy.isInitialized()) {
            this.getBluetoothScanCoordinator().shutdown();
        }
        try {
            this.tone.release();
        }
        catch (Throwable err) {
            Log.w((String)TAG, (String)"Failed to release ToneGenerator", (Throwable)err);
        }
        super.onCleared();
    }

    public final void bindAutoBrightnessWindow(@Nullable Window window) {
        this.autoBrightnessBinding.bindWindow(window, ((UiState)this._ui.getValue()).getScreenBrightness(), ((UiState)this._ui.getValue()).getAutoBrightnessEnabled());
    }

    public final void setAutoBrightnessEnabled(boolean enabled) {
        this.autoBrightnessBinding.setEnabled(enabled);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, enabled, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ boolean $enabled;
            {
                this.this$0 = $receiver;
                this.$enabled = $enabled;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).setAutoBrightnessEnabled(this.$enabled, (Continuation<? super Unit>)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void setScreenBrightness(float brightness) {
        this.autoBrightnessBinding.setBrightness(brightness);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, brightness, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ float $brightness;
            {
                this.this$0 = $receiver;
                this.$brightness = $brightness;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).setScreenBrightness(this.$brightness, (Continuation<? super Unit>)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void setHasStartedOnce(boolean value) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        MutableStateFlow<UiState> $this$update$iv = this._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, TrackingUiState.copy$default(it.getTracking(), false, false, false, null, null, null, value, false, null, 447, null), null, null, null, null, null, null, 253, null))));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this)), (CoroutineContext)((CoroutineContext)this.coroutineExceptionHandler), null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, value, null){
            int label;
            final /* synthetic */ TeamCompassViewModel this$0;
            final /* synthetic */ boolean $value;
            {
                this.this$0 = $receiver;
                this.$value = $value;
                super(2, $completion);
            }

            /*
             * WARNING - void declaration
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object object) {
                Object object2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)object);
                        this.label = 1;
                        Object object3 = TeamCompassViewModel.access$getPrefs$p(this.this$0).setHasStartedOnce(this.$value, (Continuation<? super Unit>)((Continuation)this));
                        if (object3 != object2) return Unit.INSTANCE;
                        return object2;
                    }
                    case 1: {
                        void $result;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)2, null);
    }

    public final void autoStartTrackingIfNeeded() {
        if (!((UiState)this._ui.getValue()).getHasStartedOnce() && !((UiState)this._ui.getValue()).isTracking()) {
            this.setHasStartedOnce(true);
            TeamCompassViewModel.startTracking$default(this, ((UiState)this._ui.getValue()).getDefaultMode(), false, 2, null);
        }
    }

    public final boolean hasBluetoothPermission() {
        return this.getBluetoothScanCoordinator().hasBluetoothPermission();
    }

    public final void startBluetoothScan() {
        this.getBluetoothScanCoordinator().startScan();
    }

    public final void cancelBluetoothScan() {
        this.getBluetoothScanCoordinator().cancelScan();
    }

    private static final Unit autoBrightnessBinding$lambda$1(Throwable err) {
        Intrinsics.checkNotNullParameter((Object)err, (String)"err");
        Log.w((String)TAG, (String)"Failed to initialize ScreenAutoBrightness", (Throwable)err);
        return Unit.INSTANCE;
    }

    private static final Unit headingSensorCoordinator_delegate$lambda$6$lambda$5(TeamCompassViewModel this$0, Double heading) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        this$0.trackingController.updateHeading(heading);
        MutableStateFlow<UiState> $this$update$iv = this$0._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, TrackingUiState.copy$default(it.getTracking(), false, false, false, null, heading, null, false, false, null, 495, null), null, null, null, null, null, null, 253, null))));
        return Unit.INSTANCE;
    }

    private static final HeadingSensorCoordinator headingSensorCoordinator_delegate$lambda$6(TeamCompassViewModel this$0) {
        SensorManager sensorManager = (SensorManager)this$0.application.getSystemService(SensorManager.class);
        SensorManager sensorManager2 = sensorManager;
        return new HeadingSensorCoordinator(sensorManager, (DisplayManager)this$0.application.getSystemService(DisplayManager.class), (Sensor)(sensorManager2 != null ? sensorManager2.getDefaultSensor(11) : null), (Function1<? super Double, Unit>)((Function1)arg_0 -> TeamCompassViewModel.headingSensorCoordinator_delegate$lambda$6$lambda$5(this$0, arg_0)));
    }

    private static final UiState bluetoothScanCoordinatorLazy$lambda$10$lambda$7(TeamCompassViewModel this$0) {
        return (UiState)this$0._ui.getValue();
    }

    private static final Unit bluetoothScanCoordinatorLazy$lambda$10$lambda$8(TeamCompassViewModel this$0, Function1 transform) {
        Object nextValue$iv;
        Object prevValue$iv;
        Intrinsics.checkNotNullParameter((Object)transform, (String)"transform");
        MutableStateFlow<UiState> $this$update$iv = this$0._ui;
        boolean $i$f$update = false;
        while (!$this$update$iv.compareAndSet(prevValue$iv = $this$update$iv.getValue(), nextValue$iv = transform.invoke(prevValue$iv))) {
        }
        return Unit.INSTANCE;
    }

    private static final Unit bluetoothScanCoordinatorLazy$lambda$10$lambda$9(TeamCompassViewModel this$0, String message) {
        Intrinsics.checkNotNullParameter((Object)message, (String)"message");
        TeamCompassViewModel.emitError$default(this$0, message, null, 2, null);
        return Unit.INSTANCE;
    }

    private static final BluetoothScanCoordinator bluetoothScanCoordinatorLazy$lambda$10(TeamCompassViewModel this$0) {
        return new BluetoothScanCoordinator(this$0.application, this$0.teamRepository, ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this$0)), () -> TeamCompassViewModel.bluetoothScanCoordinatorLazy$lambda$10$lambda$7(this$0), arg_0 -> TeamCompassViewModel.bluetoothScanCoordinatorLazy$lambda$10$lambda$8(this$0, arg_0), arg_0 -> TeamCompassViewModel.bluetoothScanCoordinatorLazy$lambda$10$lambda$9(this$0, arg_0), null, 64, null);
    }

    private static final UiState tacticalActionsCoordinator_delegate$lambda$14$lambda$11(TeamCompassViewModel this$0) {
        return (UiState)this$0._ui.getValue();
    }

    private static final Unit tacticalActionsCoordinator_delegate$lambda$14$lambda$12(TeamCompassViewModel this$0, Function1 transform) {
        Object nextValue$iv;
        Object prevValue$iv;
        Intrinsics.checkNotNullParameter((Object)transform, (String)"transform");
        MutableStateFlow<UiState> $this$update$iv = this$0._ui;
        boolean $i$f$update = false;
        while (!$this$update$iv.compareAndSet(prevValue$iv = $this$update$iv.getValue(), nextValue$iv = transform.invoke(prevValue$iv))) {
        }
        return Unit.INSTANCE;
    }

    private static final Unit tacticalActionsCoordinator_delegate$lambda$14$lambda$13(TeamCompassViewModel this$0, String message) {
        Intrinsics.checkNotNullParameter((Object)message, (String)"message");
        TeamCompassViewModel.emitError$default(this$0, message, null, 2, null);
        return Unit.INSTANCE;
    }

    private static final TacticalActionsCoordinator tacticalActionsCoordinator_delegate$lambda$14(TeamCompassViewModel this$0) {
        return new TacticalActionsCoordinator(this$0.teamRepository, ViewModelKt.getViewModelScope((ViewModel)((ViewModel)this$0)), () -> TeamCompassViewModel.tacticalActionsCoordinator_delegate$lambda$14$lambda$11(this$0), arg_0 -> TeamCompassViewModel.tacticalActionsCoordinator_delegate$lambda$14$lambda$12(this$0, arg_0), arg_0 -> TeamCompassViewModel.tacticalActionsCoordinator_delegate$lambda$14$lambda$13(this$0, arg_0), (Function2)new Function2<String, TeamActionFailure, Unit>((Object)this$0){

            public final void invoke(String p0, TeamActionFailure p1) {
                Intrinsics.checkNotNullParameter((Object)p0, (String)"p0");
                Intrinsics.checkNotNullParameter((Object)p1, (String)"p1");
                TeamCompassViewModel.access$handleActionFailure((TeamCompassViewModel)((Object)this.receiver), p0, p1);
            }
        }, (Function1)new Function1<String, String>((Object)this$0){

            public final String invoke(String p0) {
                Intrinsics.checkNotNullParameter((Object)p0, (String)"p0");
                return TeamCompassViewModel.access$newTraceId((TeamCompassViewModel)((Object)this.receiver), p0);
            }
        }, (Function4)new Function4<String, String, String, String, Unit>((Object)this$0){

            public final void invoke(String p0, String p1, String p2, String p3) {
                Intrinsics.checkNotNullParameter((Object)p0, (String)"p0");
                Intrinsics.checkNotNullParameter((Object)p1, (String)"p1");
                TeamCompassViewModel.access$logActionStart((TeamCompassViewModel)((Object)this.receiver), p0, p1, p2, p3);
            }
        }, (Function4)new Function4<String, String, String, String, Unit>((Object)this$0){

            public final void invoke(String p0, String p1, String p2, String p3) {
                Intrinsics.checkNotNullParameter((Object)p0, (String)"p0");
                Intrinsics.checkNotNullParameter((Object)p1, (String)"p1");
                TeamCompassViewModel.access$logActionSuccess((TeamCompassViewModel)((Object)this.receiver), p0, p1, p2, p3);
            }
        }, (Function6)new Function6<String, String, Throwable, String, String, String, Unit>((Object)this$0){

            public final void invoke(String p0, String p1, Throwable p2, String p3, String p4, String p5) {
                Intrinsics.checkNotNullParameter((Object)p0, (String)"p0");
                Intrinsics.checkNotNullParameter((Object)p1, (String)"p1");
                TeamCompassViewModel.access$logActionFailure((TeamCompassViewModel)((Object)this.receiver), p0, p1, p2, p3, p4, p5);
            }
        }, 0L, 1024, null);
    }

    private static final Unit ensureAuth$lambda$17(TeamCompassViewModel this$0, String $traceId, String uid) {
        this$0.onAuthReady(uid);
        TeamCompassViewModel.logActionSuccess$default(this$0, "ensureAuth", $traceId, null, uid, 4, null);
        return Unit.INSTANCE;
    }

    private static final Unit ensureAuth$lambda$19(TeamCompassViewModel this$0, String $traceId, Throwable err) {
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        Intrinsics.checkNotNullParameter((Object)err, (String)"err");
        MutableStateFlow<UiState> $this$update$iv = this$0._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, AuthState.copy$default(it.getAuth(), true, null, 2, null), null, null, null, null, null, null, null, 126, null))));
        Object[] objectArray = new Object[1];
        String string2 = err.getMessage();
        if (string2 == null) {
            string2 = "";
        }
        objectArray[0] = string2;
        this$0.emitError(this$0.tr(R.string.vm_error_auth_failed_format, objectArray), err);
        TeamCompassViewModel.logActionFailure$default(this$0, "ensureAuth", $traceId, err, err.getMessage(), null, null, 48, null);
        return Unit.INSTANCE;
    }

    private static final Unit refreshLocationPreview$lambda$33(TeamCompassViewModel this$0, Location loc) {
        Double d;
        double d2;
        double d3;
        double d4;
        double d5;
        TrackingUiState trackingUiState;
        UiState it;
        UiState nextValue$iv;
        Object prevValue$iv;
        if (loc == null) {
            return Unit.INSTANCE;
        }
        MutableStateFlow<UiState> $this$update$iv = this$0._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            it = (UiState)prevValue$iv;
            boolean bl = false;
            trackingUiState = it.getTracking();
            d5 = loc.getLatitude();
            d4 = loc.getLongitude();
            d3 = loc.getAccuracy();
            d2 = loc.getSpeed();
            d = it.getMyHeadingDeg();
            if (d == null) {
                d = loc.hasBearing() ? Double.valueOf(loc.getBearing()) : null;
            }
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = UiState.copy$default(it, null, TrackingUiState.copy$default(trackingUiState, false, false, false, new LocationPoint(d5, d4, d3, d2, d, System.currentTimeMillis()), null, null, false, false, null, 503, null), null, null, null, null, null, null, 253, null))));
        TeamCompassViewModel.refreshTargetsFromState$default(this$0, 0L, 1, null);
        return Unit.INSTANCE;
    }

    private static final void refreshLocationPreview$lambda$34(Function1 $tmp0, Object p0) {
        $tmp0.invoke(p0);
    }

    private static final void refreshLocationPreview$lambda$35(Exception e) {
        Intrinsics.checkNotNullParameter((Object)e, (String)"e");
        Log.w((String)TAG, (String)"lastLocation failed", (Throwable)e);
    }

    private static final TargetFilterState setTargetPreset$lambda$38(TargetFilterPreset $preset, TargetFilterState it) {
        Intrinsics.checkNotNullParameter((Object)it, (String)"it");
        return TargetFilterState.copy$default((TargetFilterState)it, (TargetFilterPreset)$preset, (int)0, (boolean)false, (boolean)false, (boolean)false, (int)30, null);
    }

    private static final TargetFilterState setNearRadius$lambda$39(int $safeRadius, TargetFilterState it) {
        Intrinsics.checkNotNullParameter((Object)it, (String)"it");
        return TargetFilterState.copy$default((TargetFilterState)it, null, (int)$safeRadius, (boolean)false, (boolean)false, (boolean)false, (int)29, null);
    }

    private static final TargetFilterState setShowDead$lambda$40(boolean $showDead, TargetFilterState it) {
        Intrinsics.checkNotNullParameter((Object)it, (String)"it");
        return TargetFilterState.copy$default((TargetFilterState)it, null, (int)0, (boolean)$showDead, (boolean)false, (boolean)false, (int)27, null);
    }

    private static final TargetFilterState setShowStale$lambda$41(boolean $showStale, TargetFilterState it) {
        Intrinsics.checkNotNullParameter((Object)it, (String)"it");
        return TargetFilterState.copy$default((TargetFilterState)it, null, (int)0, (boolean)false, (boolean)$showStale, (boolean)false, (int)23, null);
    }

    private static final TargetFilterState setFocusMode$lambda$42(boolean $enabled, TargetFilterState it) {
        Intrinsics.checkNotNullParameter((Object)it, (String)"it");
        return TargetFilterState.copy$default((TargetFilterState)it, null, (int)0, (boolean)false, (boolean)false, (boolean)$enabled, (int)15, null);
    }

    private static final String assignTeamMemberRolesBulk$lambda$51(String it) {
        Intrinsics.checkNotNullParameter((Object)it, (String)"it");
        return ((Object)StringsKt.trim((CharSequence)it)).toString();
    }

    private static final boolean assignTeamMemberRolesBulk$lambda$52(String $actorUid, String it) {
        Intrinsics.checkNotNullParameter((Object)it, (String)"it");
        return !StringsKt.isBlank((CharSequence)it) && !Intrinsics.areEqual((Object)it, (Object)$actorUid);
    }

    private static final TeamViewMode collectTeamSnapshotsWithReconnect$lambda$69(TeamCompassViewModel this$0) {
        return ((UiState)this$0._ui.getValue()).getViewMode();
    }

    private static final LocationPoint collectTeamSnapshotsWithReconnect$lambda$70(TeamCompassViewModel this$0) {
        return ((UiState)this$0._ui.getValue()).getMe();
    }

    private static final long scheduleBackendStaleRefresh$lambda$73(TeamCompassViewModel this$0) {
        return ((UiState)this$0._ui.getValue()).getTracking().getTelemetry().getLastSnapshotAtMs();
    }

    private static final Unit scheduleBackendStaleRefresh$lambda$74(TeamCompassViewModel this$0, long refreshedAtMs) {
        this$0.refreshBackendStaleFlag(refreshedAtMs);
        return Unit.INSTANCE;
    }

    private static final TeamMemberPrefs startMemberPrefsSync$lambda$79(TeamCompassViewModel this$0, TargetFilterState filterState) {
        Intrinsics.checkNotNullParameter((Object)filterState, (String)"filterState");
        return this$0.targetFilterCoordinator.toMemberPrefs(filterState);
    }

    private static final Unit startMemberPrefsSync$lambda$81(TeamCompassViewModel this$0, TeamMemberPrefs remotePrefs) {
        UiState state;
        UiState nextValue$iv;
        Object prevValue$iv;
        TargetFilterState next = this$0.targetFilterCoordinator.fromRemotePrefs(remotePrefs);
        MutableStateFlow<UiState> $this$update$iv = this$0._ui;
        boolean $i$f$update = false;
        do {
            prevValue$iv = $this$update$iv.getValue();
            state = (UiState)prevValue$iv;
            boolean bl = false;
        } while (!$this$update$iv.compareAndSet(prevValue$iv, (Object)(nextValue$iv = Intrinsics.areEqual((Object)state.getFilter().getTargetFilterState(), (Object)next) ? state : UiState.copy$default(state, null, null, null, null, FilterUiState.copy$default(state.getFilter(), next, null, null, 6, null), null, null, null, 239, null))));
        TeamCompassViewModel.refreshTargetsFromState$default(this$0, 0L, 1, null);
        return Unit.INSTANCE;
    }

    private static final boolean startMemberPrefsSync$lambda$82(TeamCompassViewModel this$0) {
        return this$0.targetFilterDirtyByUser;
    }

    private static final Unit startMemberPrefsSync$lambda$83(TeamCompassViewModel this$0) {
        this$0.targetFilterDirtyByUser = false;
        return Unit.INSTANCE;
    }

    private static final Unit startMemberPrefsSync$lambda$84(TeamCompassViewModel this$0, TeamActionFailure failure, boolean userInitiated) {
        Intrinsics.checkNotNullParameter((Object)failure, (String)"failure");
        if (userInitiated) {
            this$0.emitError(this$0.tr(R.string.vm_error_targets_filter_save_failed, new Object[0]), failure.getCause());
        } else {
            Log.w((String)TAG, (String)("Background memberPrefs sync failed: " + failure.getError()));
        }
        return Unit.INSTANCE;
    }

    private static final UiState startLocationServiceMonitor$lambda$96(TeamCompassViewModel this$0) {
        return (UiState)this$0._ui.getValue();
    }

    private static final Unit startLocationServiceMonitor$lambda$97(TeamCompassViewModel this$0, Function1 transform) {
        Object nextValue$iv;
        Object prevValue$iv;
        Intrinsics.checkNotNullParameter((Object)transform, (String)"transform");
        MutableStateFlow<UiState> $this$update$iv = this$0._ui;
        boolean $i$f$update = false;
        while (!$this$update$iv.compareAndSet(prevValue$iv = $this$update$iv.getValue(), nextValue$iv = transform.invoke(prevValue$iv))) {
        }
        return Unit.INSTANCE;
    }

    private static final Unit startLocationServiceMonitor$lambda$98(TeamCompassViewModel this$0, String message) {
        Intrinsics.checkNotNullParameter((Object)message, (String)"message");
        TeamCompassViewModel.emitError$default(this$0, message, null, 2, null);
        return Unit.INSTANCE;
    }

    public static final /* synthetic */ UserPrefs access$getPrefs$p(TeamCompassViewModel $this) {
        return $this.prefs;
    }

    public static final /* synthetic */ MutableStateFlow access$get_ui$p(TeamCompassViewModel $this) {
        return $this._ui;
    }

    public static final /* synthetic */ String access$normalizeTeamCode(TeamCompassViewModel $this, String raw) {
        return $this.normalizeTeamCode(raw);
    }

    public static final /* synthetic */ void access$startListening(TeamCompassViewModel $this, String codeRaw) {
        $this.startListening(codeRaw);
    }

    public static final /* synthetic */ void access$stopListening(TeamCompassViewModel $this) {
        $this.stopListening();
    }

    public static final /* synthetic */ AutoBrightnessBinding access$getAutoBrightnessBinding$p(TeamCompassViewModel $this) {
        return $this.autoBrightnessBinding;
    }

    public static final /* synthetic */ TrackingController access$getTrackingController$p(TeamCompassViewModel $this) {
        return $this.trackingController;
    }

    public static final /* synthetic */ MapCoordinator access$getMapCoordinator$p(TeamCompassViewModel $this) {
        return $this.mapCoordinator;
    }

    public static final /* synthetic */ Application access$getApplication$p(TeamCompassViewModel $this) {
        return $this.application;
    }

    public static final /* synthetic */ String access$tr(TeamCompassViewModel $this, int resId, Object ... args) {
        return $this.tr(resId, args);
    }

    public static final /* synthetic */ TeamRepository access$getTeamRepository$p(TeamCompassViewModel $this) {
        return $this.teamRepository;
    }

    public static final /* synthetic */ void access$handleActionFailure(TeamCompassViewModel $this, String defaultMessage, TeamActionFailure failure) {
        $this.handleActionFailure(defaultMessage, failure);
    }

    public static final /* synthetic */ TeamSessionDelegate access$getTeamSessionDelegate$p(TeamCompassViewModel $this) {
        return $this.teamSessionDelegate;
    }

    public static final /* synthetic */ Object access$onTeamJoined(TeamCompassViewModel $this, String code, Continuation $completion) {
        return $this.onTeamJoined(code, (Continuation<? super Unit>)$completion);
    }

    public static final /* synthetic */ void access$evaluateIdentityLinkingEligibility(TeamCompassViewModel $this, String teamCode) {
        $this.evaluateIdentityLinkingEligibility(teamCode);
    }

    public static final /* synthetic */ void access$logActionSuccess(TeamCompassViewModel $this, String action, String traceId, String teamCode, String uid) {
        $this.logActionSuccess(action, traceId, teamCode, uid);
    }

    public static final /* synthetic */ void access$logActionFailure(TeamCompassViewModel $this, String action, String traceId, Throwable throwable, String message, String teamCode, String uid) {
        $this.logActionFailure(action, traceId, throwable, message, teamCode, uid);
    }

    public static final /* synthetic */ boolean access$handleStartListeningTerminalFailure(TeamCompassViewModel $this, TeamActionFailure failure, String teamCode, String uid, String traceId) {
        return $this.handleStartListeningTerminalFailure(failure, teamCode, uid, traceId);
    }

    public static final /* synthetic */ void access$startBackendHealthMonitor(TeamCompassViewModel $this) {
        $this.startBackendHealthMonitor();
    }

    public static final /* synthetic */ void access$startBackendStaleMonitor(TeamCompassViewModel $this) {
        $this.startBackendStaleMonitor();
    }

    public static final /* synthetic */ void access$startP2PInboundObservation(TeamCompassViewModel $this, String teamCode, String localUid) {
        $this.startP2PInboundObservation(teamCode, localUid);
    }

    public static final /* synthetic */ Object access$collectTeamSnapshotsWithReconnect(TeamCompassViewModel $this, String teamCode, String uid, Continuation $completion) {
        return $this.collectTeamSnapshotsWithReconnect(teamCode, uid, (Continuation<? super Unit>)$completion);
    }

    public static final /* synthetic */ void access$scheduleBackendStaleRefresh(TeamCompassViewModel $this, long nowMs) {
        $this.scheduleBackendStaleRefresh(nowMs);
    }

    public static final /* synthetic */ void access$processEnemyPingAlerts(TeamCompassViewModel $this, List enemyPings) {
        $this.processEnemyPingAlerts(enemyPings);
    }

    public static final /* synthetic */ void access$processSosAlerts(TeamCompassViewModel $this) {
        $this.processSosAlerts();
    }

    public static final /* synthetic */ boolean access$computeBackendStale(TeamCompassViewModel $this, long lastSnapshotAtMs, long nowMs) {
        return $this.computeBackendStale(lastSnapshotAtMs, nowMs);
    }

    public static final /* synthetic */ void access$handleP2PInbound(TeamCompassViewModel $this, P2PInboundMessage inbound, String localUid) {
        $this.handleP2PInbound(inbound, localUid);
    }

    public static final /* synthetic */ String access$newTraceId(TeamCompassViewModel $this, String action) {
        return $this.newTraceId(action);
    }

    public static final /* synthetic */ void access$setLastBackendHealthAvailableSample$p(TeamCompassViewModel $this, boolean bl) {
        $this.lastBackendHealthAvailableSample = bl;
    }

    public static final /* synthetic */ void access$vibrateAndBeep(TeamCompassViewModel $this, boolean strong) {
        $this.vibrateAndBeep(strong);
    }

    public static final /* synthetic */ void access$logActionStart(TeamCompassViewModel $this, String action, String traceId, String teamCode, String uid) {
        $this.logActionStart(action, traceId, teamCode, uid);
    }

    public static final /* synthetic */ Function2 access$getInitializeAutoStartOverride$p(TeamCompassViewModel $this) {
        return $this.initializeAutoStartOverride;
    }

    public static final /* synthetic */ void access$initializeAutoStart(TeamCompassViewModel $this) {
        $this.initializeAutoStart();
    }

    @Metadata(mv={2, 0, 0}, k=1, xi=48, d1={"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0015\u0010\u0012\u001a\u00020\b2\u0006\u0010\u0013\u001a\u00020\u0014H\u0000\u00a2\u0006\u0002\b\u0015J\u0012\u0010\u0016\u001a\u0004\u0018\u00010\u00172\u0006\u0010\u0018\u001a\u00020\u0005H\u0002J\u0012\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\u0006\u0010\u0018\u001a\u00020\u0005H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\bX\u0080T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2={"Lcom/example/teamcompass/ui/TeamCompassViewModel$Companion;", "", "<init>", "()V", "TAG", "", "INIT_FAILURE_MESSAGE", "OP_TIMEOUT_MS", "", "SOS_DURATION_MS", "TEAM_RECONNECT_INITIAL_DELAY_MS", "TEAM_RECONNECT_MAX_DELAY_MS", "STALE_WARNING_MS", "STATE_TEAM_CODE", "STATE_DEFAULT_MODE", "STATE_PLAYER_MODE", "STATE_IS_TRACKING", "STATE_MY_SOS_UNTIL_MS", "locationServicePollIntervalMs", "isTracking", "", "locationServicePollIntervalMs$app_debug", "parseTrackingModeOrNull", "Lcom/example/teamcompass/core/TrackingMode;", "value", "parsePlayerModeOrNull", "Lcom/example/teamcompass/core/PlayerMode;", "app_debug"})
    @SourceDebugExtension(value={"SMAP\nTeamCompassViewModel.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TeamCompassViewModel.kt\ncom/example/teamcompass/ui/TeamCompassViewModel$Companion\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,1920:1\n1#2:1921\n*E\n"})
    public static final class Companion {
        private Companion() {
        }

        public final long locationServicePollIntervalMs$app_debug(boolean isTracking) {
            return isTracking ? 2000L : 12000L;
        }

        private final TrackingMode parseTrackingModeOrNull(String value) {
            Object object;
            Object object2 = this;
            try {
                Companion $this$parseTrackingModeOrNull_u24lambda_u240 = object2;
                boolean bl = false;
                object = Result.constructor-impl((Object)TrackingMode.valueOf((String)value));
            }
            catch (Throwable throwable) {
                object = Result.constructor-impl((Object)ResultKt.createFailure((Throwable)throwable));
            }
            object2 = object;
            return (TrackingMode)(Result.isFailure-impl((Object)object2) ? null : object2);
        }

        private final PlayerMode parsePlayerModeOrNull(String value) {
            Object object;
            Object object2 = this;
            try {
                Companion $this$parsePlayerModeOrNull_u24lambda_u241 = object2;
                boolean bl = false;
                object = Result.constructor-impl((Object)PlayerMode.valueOf((String)value));
            }
            catch (Throwable throwable) {
                object = Result.constructor-impl((Object)ResultKt.createFailure((Throwable)throwable));
            }
            object2 = object;
            return (PlayerMode)(Result.isFailure-impl((Object)object2) ? null : object2);
        }

        public static final /* synthetic */ TrackingMode access$parseTrackingModeOrNull(Companion $this, String value) {
            return $this.parseTrackingModeOrNull(value);
        }

        public static final /* synthetic */ PlayerMode access$parsePlayerModeOrNull(Companion $this, String value) {
            return $this.parsePlayerModeOrNull(value);
        }

        public /* synthetic */ Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }
    }

    @Metadata(mv={2, 0, 0}, k=1, xi=48, d1={"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0014\n\u0002\u0010\b\n\u0002\b\u0002\b\u0082\b\u0018\u00002\u00020\u0001B1\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0004\b\f\u0010\rJ\u000b\u0010\u0017\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\tH\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u000bH\u00c6\u0003J=\u0010\u001c\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u000bH\u00c6\u0001J\u0013\u0010\u001d\u001a\u00020\t2\b\u0010\u001e\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001f\u001a\u00020 H\u00d6\u0001J\t\u0010!\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0014R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016\u00a8\u0006\""}, d2={"Lcom/example/teamcompass/ui/TeamCompassViewModel$RestorableVmState;", "", "teamCode", "", "defaultMode", "Lcom/example/teamcompass/core/TrackingMode;", "playerMode", "Lcom/example/teamcompass/core/PlayerMode;", "isTracking", "", "mySosUntilMs", "", "<init>", "(Ljava/lang/String;Lcom/example/teamcompass/core/TrackingMode;Lcom/example/teamcompass/core/PlayerMode;ZJ)V", "getTeamCode", "()Ljava/lang/String;", "getDefaultMode", "()Lcom/example/teamcompass/core/TrackingMode;", "getPlayerMode", "()Lcom/example/teamcompass/core/PlayerMode;", "()Z", "getMySosUntilMs", "()J", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
    private static final class RestorableVmState {
        @Nullable
        private final String teamCode;
        @NotNull
        private final TrackingMode defaultMode;
        @NotNull
        private final PlayerMode playerMode;
        private final boolean isTracking;
        private final long mySosUntilMs;

        public RestorableVmState(@Nullable String teamCode, @NotNull TrackingMode defaultMode, @NotNull PlayerMode playerMode, boolean isTracking, long mySosUntilMs) {
            Intrinsics.checkNotNullParameter((Object)defaultMode, (String)"defaultMode");
            Intrinsics.checkNotNullParameter((Object)playerMode, (String)"playerMode");
            this.teamCode = teamCode;
            this.defaultMode = defaultMode;
            this.playerMode = playerMode;
            this.isTracking = isTracking;
            this.mySosUntilMs = mySosUntilMs;
        }

        @Nullable
        public final String getTeamCode() {
            return this.teamCode;
        }

        @NotNull
        public final TrackingMode getDefaultMode() {
            return this.defaultMode;
        }

        @NotNull
        public final PlayerMode getPlayerMode() {
            return this.playerMode;
        }

        public final boolean isTracking() {
            return this.isTracking;
        }

        public final long getMySosUntilMs() {
            return this.mySosUntilMs;
        }

        @Nullable
        public final String component1() {
            return this.teamCode;
        }

        @NotNull
        public final TrackingMode component2() {
            return this.defaultMode;
        }

        @NotNull
        public final PlayerMode component3() {
            return this.playerMode;
        }

        public final boolean component4() {
            return this.isTracking;
        }

        public final long component5() {
            return this.mySosUntilMs;
        }

        @NotNull
        public final RestorableVmState copy(@Nullable String teamCode, @NotNull TrackingMode defaultMode, @NotNull PlayerMode playerMode, boolean isTracking, long mySosUntilMs) {
            Intrinsics.checkNotNullParameter((Object)defaultMode, (String)"defaultMode");
            Intrinsics.checkNotNullParameter((Object)playerMode, (String)"playerMode");
            return new RestorableVmState(teamCode, defaultMode, playerMode, isTracking, mySosUntilMs);
        }

        public static /* synthetic */ RestorableVmState copy$default(RestorableVmState restorableVmState, String string2, TrackingMode trackingMode, PlayerMode playerMode, boolean bl, long l, int n, Object object) {
            if ((n & 1) != 0) {
                string2 = restorableVmState.teamCode;
            }
            if ((n & 2) != 0) {
                trackingMode = restorableVmState.defaultMode;
            }
            if ((n & 4) != 0) {
                playerMode = restorableVmState.playerMode;
            }
            if ((n & 8) != 0) {
                bl = restorableVmState.isTracking;
            }
            if ((n & 0x10) != 0) {
                l = restorableVmState.mySosUntilMs;
            }
            return restorableVmState.copy(string2, trackingMode, playerMode, bl, l);
        }

        @NotNull
        public String toString() {
            return "RestorableVmState(teamCode=" + this.teamCode + ", defaultMode=" + this.defaultMode + ", playerMode=" + this.playerMode + ", isTracking=" + this.isTracking + ", mySosUntilMs=" + this.mySosUntilMs + ")";
        }

        public int hashCode() {
            int result = this.teamCode == null ? 0 : this.teamCode.hashCode();
            result = result * 31 + this.defaultMode.hashCode();
            result = result * 31 + this.playerMode.hashCode();
            result = result * 31 + Boolean.hashCode(this.isTracking);
            result = result * 31 + Long.hashCode(this.mySosUntilMs);
            return result;
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof RestorableVmState)) {
                return false;
            }
            RestorableVmState restorableVmState = (RestorableVmState)other;
            if (!Intrinsics.areEqual((Object)this.teamCode, (Object)restorableVmState.teamCode)) {
                return false;
            }
            if (this.defaultMode != restorableVmState.defaultMode) {
                return false;
            }
            if (this.playerMode != restorableVmState.playerMode) {
                return false;
            }
            if (this.isTracking != restorableVmState.isTracking) {
                return false;
            }
            return this.mySosUntilMs == restorableVmState.mySosUntilMs;
        }
    }

    @Metadata(mv={2, 0, 0}, k=3, xi=48)
    public final class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;

        static {
            int[] nArray = new int[TeamActionError.values().length];
            try {
                nArray[TeamActionError.LOCKED.ordinal()] = 1;
            }
            catch (NoSuchFieldError noSuchFieldError) {
                // empty catch block
            }
            try {
                nArray[TeamActionError.EXPIRED.ordinal()] = 2;
            }
            catch (NoSuchFieldError noSuchFieldError) {
                // empty catch block
            }
            try {
                nArray[TeamActionError.NOT_FOUND.ordinal()] = 3;
            }
            catch (NoSuchFieldError noSuchFieldError) {
                // empty catch block
            }
            try {
                nArray[TeamActionError.PERMISSION_DENIED.ordinal()] = 4;
            }
            catch (NoSuchFieldError noSuchFieldError) {
                // empty catch block
            }
            $EnumSwitchMapping$0 = nArray;
        }
    }
}
