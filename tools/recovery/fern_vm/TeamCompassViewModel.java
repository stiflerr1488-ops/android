package com.example.teamcompass.ui;

import android.app.Application;
import android.content.Context;
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
import com.example.teamcompass.R.string;
import com.example.teamcompass.auth.FirebaseIdentityLinkingService;
import com.example.teamcompass.auth.IdentityLinkingEligibility;
import com.example.teamcompass.auth.IdentityLinkingService;
import com.example.teamcompass.auth.NoOpIdentityLinkingService;
import com.example.teamcompass.bluetooth.BluetoothScanner;
import com.example.teamcompass.core.CompassCalculator;
import com.example.teamcompass.core.LocationPoint;
import com.example.teamcompass.core.PlayerMode;
import com.example.teamcompass.core.PlayerState;
import com.example.teamcompass.core.TargetFilterPreset;
import com.example.teamcompass.core.TargetFilterState;
import com.example.teamcompass.core.TrackingMode;
import com.example.teamcompass.domain.TeamActionFailure;
import com.example.teamcompass.domain.TeamMemberPrefs;
import com.example.teamcompass.domain.TeamRepository;
import com.example.teamcompass.domain.TeamRolePatch;
import com.example.teamcompass.domain.TeamViewMode;
import com.example.teamcompass.domain.TrackingController;
import com.example.teamcompass.p2p.P2PInboundMessage;
import com.example.teamcompass.p2p.P2PTransportManager;
import com.example.teamcompass.perf.TeamCompassPerfMetrics;
import com.example.teamcompass.perf.TeamCompassPerfSnapshot;
import com.example.teamcompass.ui.theme.ThemeMode;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import javax.inject.Inject;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.Metadata;
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
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.functions.Function4;
import kotlin.jvm.functions.Function6;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.ranges.RangesKt;
import kotlin.sequences.SequencesKt;
import kotlin.text.StringsKt;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineExceptionHandler;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.Job.DefaultImpls;
import kotlinx.coroutines.channels.BufferOverflow;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowKt;
import kotlinx.coroutines.flow.MutableSharedFlow;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.SharedFlow;
import kotlinx.coroutines.flow.SharedFlowKt;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(
   mv = {2, 0, 0},
   k = 1,
   xi = 48,
   d1 = {"\u0000ø\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\u0010\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010\u0003\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u0006\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u0011\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u000b\b\u0007\u0018\u0000 ´\u00022\u00020\u0001:\u0004´\u0002µ\u0002Bª\u0001\b\u0000\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0011\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0013\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0015\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0017\u0012+\b\u0002\u0010\u0018\u001a%\b\u0001\u0012\u0004\u0012\u00020\u0000\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001b0\u001a\u0012\u0006\u0012\u0004\u0018\u00010\u001c\u0018\u00010\u0019¢\u0006\u0002\b\u001d\u0012\n\b\u0002\u0010\u001e\u001a\u0004\u0018\u00010\u001f¢\u0006\u0004\b \u0010!BI\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\u0012\u001a\u00020\u0013\u0012\u0006\u0010\u0016\u001a\u00020\u0017\u0012\u0006\u0010\u001e\u001a\u00020\u001f¢\u0006\u0004\b \u0010\"J\u0010\u0010~\u001a\u00020R2\u0006\u0010\u007f\u001a\u00020RH\u0002J4\u0010\u0080\u0001\u001a\u00020\u001b2\u0006\u0010\u007f\u001a\u00020R2\u0007\u0010\u0081\u0001\u001a\u00020R2\u000b\b\u0002\u0010\u0082\u0001\u001a\u0004\u0018\u00010R2\u000b\b\u0002\u0010\u0083\u0001\u001a\u0004\u0018\u00010RH\u0002J4\u0010\u0084\u0001\u001a\u00020\u001b2\u0006\u0010\u007f\u001a\u00020R2\u0007\u0010\u0081\u0001\u001a\u00020R2\u000b\b\u0002\u0010\u0082\u0001\u001a\u0004\u0018\u00010R2\u000b\b\u0002\u0010\u0083\u0001\u001a\u0004\u0018\u00010RH\u0002JO\u0010\u0085\u0001\u001a\u00020\u001b2\u0006\u0010\u007f\u001a\u00020R2\u0007\u0010\u0081\u0001\u001a\u00020R2\f\b\u0002\u0010\u0086\u0001\u001a\u0005\u0018\u00010\u0087\u00012\u000b\b\u0002\u0010\u0088\u0001\u001a\u0004\u0018\u00010R2\u000b\b\u0002\u0010\u0082\u0001\u001a\u0004\u0018\u00010R2\u000b\b\u0002\u0010\u0083\u0001\u001a\u0004\u0018\u00010RH\u0002J\t\u0010\u0089\u0001\u001a\u00020\u001bH\u0002J%\u0010\u008a\u0001\u001a\u00020\u001b2\u0014\u0010\u008b\u0001\u001a\u000f\u0012\u0004\u0012\u00020a\u0012\u0004\u0012\u00020a0\u008c\u0001H\u0001¢\u0006\u0003\b\u008d\u0001J\u0018\u0010\u008e\u0001\u001a\u00020\u001b2\u0007\u0010\u008f\u0001\u001a\u00020^H\u0001¢\u0006\u0003\b\u0090\u0001J\u001e\u0010\u0091\u0001\u001a\u00020\u001b2\u0007\u0010\u0088\u0001\u001a\u00020R2\f\b\u0002\u0010\u0092\u0001\u001a\u0005\u0018\u00010\u0087\u0001J\t\u0010\u0093\u0001\u001a\u00020\u001bH\u0002J\t\u0010\u0094\u0001\u001a\u00020\u001bH\u0002J\u0007\u0010\u0095\u0001\u001a\u00020\u001bJ\u0014\u0010\u0096\u0001\u001a\u00020\u001b2\t\u0010\u0083\u0001\u001a\u0004\u0018\u00010RH\u0002J\u0010\u0010\u0097\u0001\u001a\u00020\u001b2\u0007\u0010\u0098\u0001\u001a\u00020RJ\u0010\u0010\u0099\u0001\u001a\u00020\u001b2\u0007\u0010\u009a\u0001\u001a\u00020YJ\u0011\u0010\u009b\u0001\u001a\u00020\u001b2\b\u0010\u009a\u0001\u001a\u00030\u009c\u0001J\u001b\u0010\u009d\u0001\u001a\u00020\u001b2\b\u0010\u009e\u0001\u001a\u00030\u009f\u00012\b\u0010 \u0001\u001a\u00030\u009f\u0001J\u001b\u0010¡\u0001\u001a\u00020\u001b2\b\u0010\u009e\u0001\u001a\u00030\u009f\u00012\b\u0010 \u0001\u001a\u00030\u009f\u0001J\u0011\u0010¢\u0001\u001a\u00020\u001b2\b\u0010\u009a\u0001\u001a\u00030£\u0001J\u0010\u0010¤\u0001\u001a\u00020\u001b2\u0007\u0010¥\u0001\u001a\u00020\rJ\u0007\u0010¦\u0001\u001a\u00020\u001bJ\t\u0010§\u0001\u001a\u00020\u001bH\u0002J\u0007\u0010¨\u0001\u001a\u00020\u001bJ\u0007\u0010©\u0001\u001a\u00020\u001bJ\u0010\u0010ª\u0001\u001a\u00020\u001b2\u0007\u0010\u009a\u0001\u001a\u00020[J\u0010\u0010«\u0001\u001a\u00020\u001b2\u0007\u0010¬\u0001\u001a\u00020\rJ\u0011\u0010\u00ad\u0001\u001a\u00020\u001b2\b\u0010®\u0001\u001a\u00030¯\u0001J\u0011\u0010°\u0001\u001a\u00020\u001b2\b\u0010±\u0001\u001a\u00030\u009f\u0001J\u0010\u0010²\u0001\u001a\u00020\u001b2\u0007\u0010³\u0001\u001a\u00020\rJ\u0010\u0010´\u0001\u001a\u00020\u001b2\u0007\u0010µ\u0001\u001a\u00020\rJ\u0010\u0010¶\u0001\u001a\u00020\u001b2\u0007\u0010¬\u0001\u001a\u00020\rJ!\u0010·\u0001\u001a\u00020\u001b2\u0016\u0010\u008b\u0001\u001a\u0011\u0012\u0005\u0012\u00030¸\u0001\u0012\u0005\u0012\u00030¸\u00010\u008c\u0001H\u0002J\u0011\u0010¹\u0001\u001a\u00020\u001b2\b\u0010º\u0001\u001a\u00030»\u0001J\u0007\u0010¼\u0001\u001a\u00020\u001bJ\u0010\u0010½\u0001\u001a\u00020\u001b2\u0007\u0010¬\u0001\u001a\u00020\rJ\u0011\u0010¾\u0001\u001a\u00020\u001b2\b\u0010¿\u0001\u001a\u00030À\u0001J+\u0010Á\u0001\u001a\u00020\u001b2\u000f\u0010Â\u0001\u001a\n\u0012\u0005\u0012\u00030Ä\u00010Ã\u00012\u0011\b\u0002\u0010Å\u0001\u001a\n\u0012\u0005\u0012\u00030Ä\u00010Ã\u0001J5\u0010Æ\u0001\u001a\u00020\u001b2\b\u0010º\u0001\u001a\u00030»\u00012\u000f\u0010Â\u0001\u001a\n\u0012\u0005\u0012\u00030Ä\u00010Ã\u00012\u0011\b\u0002\u0010Å\u0001\u001a\n\u0012\u0005\u0012\u00030Ä\u00010Ã\u0001J?\u0010Ç\u0001\u001a\u00020\u001b2\b\u0010È\u0001\u001a\u00030»\u00012\b\u0010É\u0001\u001a\u00030Ê\u00012\u000f\u0010Â\u0001\u001a\n\u0012\u0005\u0012\u00030Ä\u00010Ã\u00012\u000f\u0010Å\u0001\u001a\n\u0012\u0005\u0012\u00030Ä\u00010Ã\u0001H\u0002J\u0007\u0010Ë\u0001\u001a\u00020\u001bJ\u0007\u0010Ì\u0001\u001a\u00020\u001bJ\u0007\u0010Í\u0001\u001a\u00020\u001bJ\"\u0010Î\u0001\u001a\u00020\u001b2\u0007\u0010Ï\u0001\u001a\u00020R2\u0007\u0010Ð\u0001\u001a\u00020R2\u0007\u0010Ñ\u0001\u001a\u00020\rJ6\u0010Ò\u0001\u001a\u00020\u001b2\b\u0010Ó\u0001\u001a\u00030Ô\u00012\b\u0010Õ\u0001\u001a\u00030Ô\u00012\u0007\u0010Ï\u0001\u001a\u00020R2\u0007\u0010Ð\u0001\u001a\u00020R2\u0007\u0010Ñ\u0001\u001a\u00020\rJ?\u0010Ö\u0001\u001a\u00020\u001b2\u0007\u0010×\u0001\u001a\u00020R2\b\u0010Ó\u0001\u001a\u00030Ô\u00012\b\u0010Õ\u0001\u001a\u00030Ô\u00012\u0007\u0010Ï\u0001\u001a\u00020R2\u0007\u0010Ð\u0001\u001a\u00020R2\u0007\u0010Ø\u0001\u001a\u00020\rJ\u0019\u0010Ù\u0001\u001a\u00020\u001b2\u0007\u0010×\u0001\u001a\u00020R2\u0007\u0010Ø\u0001\u001a\u00020\rJ\u0011\u0010Ú\u0001\u001a\u00020\u001b2\b\u0010Û\u0001\u001a\u00030Ü\u0001J%\u0010Ý\u0001\u001a\u00020\u001b2\b\u0010Ó\u0001\u001a\u00030Ô\u00012\b\u0010Õ\u0001\u001a\u00030Ô\u00012\b\u0010Û\u0001\u001a\u00030Ü\u0001J\u001a\u0010Þ\u0001\u001a\u00020\u001b2\u0007\u0010ß\u0001\u001a\u00020R2\b\u0010à\u0001\u001a\u00030á\u0001J!\u0010â\u0001\u001a\u00020\u001b2\u000e\u0010ã\u0001\u001a\t\u0012\u0004\u0012\u00020R0Ã\u00012\b\u0010à\u0001\u001a\u00030á\u0001J\u0007\u0010ä\u0001\u001a\u00020\u001bJ\u001b\u0010å\u0001\u001a\u00020\u001b2\u0007\u0010æ\u0001\u001a\u00020R2\t\b\u0002\u0010ç\u0001\u001a\u00020\rJ\u0019\u0010è\u0001\u001a\u00020\u001b2\u0007\u0010é\u0001\u001a\u00020RH\u0082@¢\u0006\u0003\u0010ê\u0001J\u0012\u0010ë\u0001\u001a\u00020\u001b2\u0007\u0010\u0082\u0001\u001a\u00020RH\u0002J\u0007\u0010ì\u0001\u001a\u00020\u001bJ\u0007\u0010í\u0001\u001a\u00020\u001bJ\u0010\u0010î\u0001\u001a\u00020\u001b2\u0007\u0010¬\u0001\u001a\u00020\rJ\u001b\u0010ï\u0001\u001a\u00020\u001b2\b\u0010×\u0001\u001a\u00030ð\u00012\b\u0010ñ\u0001\u001a\u00030ò\u0001J\u0007\u0010ó\u0001\u001a\u00020\u001bJ\u0011\u0010ô\u0001\u001a\u00020\u001b2\b\u0010®\u0001\u001a\u00030õ\u0001J\u0007\u0010ö\u0001\u001a\u00020\u001bJ\u0012\u0010÷\u0001\u001a\u00020\u001b2\u0007\u0010æ\u0001\u001a\u00020RH\u0002J.\u0010ø\u0001\u001a\u00020\r2\b\u0010ù\u0001\u001a\u00030ú\u00012\u0007\u0010\u0082\u0001\u001a\u00020R2\u0007\u0010\u0083\u0001\u001a\u00020R2\u0007\u0010\u0081\u0001\u001a\u00020RH\u0002J\t\u0010û\u0001\u001a\u00020\u001bH\u0002J\"\u0010ü\u0001\u001a\u00020\u001b2\u0007\u0010\u0082\u0001\u001a\u00020R2\u0007\u0010\u0083\u0001\u001a\u00020RH\u0082@¢\u0006\u0003\u0010ý\u0001J\t\u0010þ\u0001\u001a\u00020\u001bH\u0002J\u001b\u0010ÿ\u0001\u001a\u00020\u001b2\u0007\u0010\u0082\u0001\u001a\u00020R2\u0007\u0010\u0080\u0002\u001a\u00020RH\u0002J\u001c\u0010\u0081\u0002\u001a\u00020\u001b2\b\u0010\u0082\u0002\u001a\u00030\u0083\u00022\u0007\u0010\u0080\u0002\u001a\u00020RH\u0002J\t\u0010\u0084\u0002\u001a\u00020\u001bH\u0002J\t\u0010\u0085\u0002\u001a\u00020\u001bH\u0002J\u0014\u0010\u0086\u0002\u001a\u00020\u001b2\t\b\u0002\u0010\u008f\u0001\u001a\u00020^H\u0002J\u0014\u0010\u0087\u0002\u001a\u00020\u001b2\t\b\u0002\u0010\u008f\u0001\u001a\u00020^H\u0002J\u001b\u0010\u0088\u0002\u001a\u00020\r2\u0007\u0010\u0089\u0002\u001a\u00020^2\u0007\u0010\u008f\u0001\u001a\u00020^H\u0002J\u001b\u0010\u008a\u0002\u001a\u00020\u001b2\u0007\u0010\u0082\u0001\u001a\u00020R2\u0007\u0010\u0083\u0001\u001a\u00020RH\u0002J\u001a\u0010\u008b\u0002\u001a\u00020\u001b2\u000f\u0010\u008c\u0002\u001a\n\u0012\u0005\u0012\u00030\u008d\u00020Ã\u0001H\u0002J\t\u0010\u008e\u0002\u001a\u00020\u001bH\u0002J\u0012\u0010\u008f\u0002\u001a\u00020\u001b2\u0007\u0010\u0090\u0002\u001a\u00020\rH\u0002J\t\u0010\u0091\u0002\u001a\u00020\u001bH\u0002J\t\u0010\u0092\u0002\u001a\u00020\u001bH\u0002J\u001b\u0010\u0093\u0002\u001a\u00020\u001b2\u0007\u0010\u009a\u0001\u001a\u00020Y2\t\b\u0002\u0010\u0094\u0002\u001a\u00020\rJ\t\u0010\u0095\u0002\u001a\u00020\u001bH\u0002J\u0007\u0010\u0096\u0002\u001a\u00020\u001bJ\u0007\u0010\u0097\u0002\u001a\u00020\u001bJ\u0018\u0010\u0098\u0002\u001a\n\u0012\u0005\u0012\u00030\u0099\u00020Ã\u00012\u0007\u0010\u008f\u0001\u001a\u00020^J\u0014\u0010\u009a\u0002\u001a\u00020\u001b2\t\b\u0002\u0010\u008f\u0001\u001a\u00020^H\u0002J\t\u0010\u009b\u0002\u001a\u00020\u001bH\u0002J\t\u0010\u009c\u0002\u001a\u00020\u001bH\u0002J\t\u0010\u009d\u0002\u001a\u00020\u001bH\u0002J\u0016\u0010\u009e\u0002\u001a\u0004\u0018\u00010R2\t\u0010\u009f\u0002\u001a\u0004\u0018\u00010RH\u0002J1\u0010 \u0002\u001a\u00020R2\n\b\u0001\u0010¡\u0002\u001a\u00030\u009f\u00012\u0014\u0010¢\u0002\u001a\u000b\u0012\u0006\b\u0001\u0012\u00020\u001c0£\u0002\"\u00020\u001cH\u0002¢\u0006\u0003\u0010¤\u0002J\u001c\u0010¥\u0002\u001a\u00020\u001b2\u0007\u0010¦\u0002\u001a\u00020R2\b\u0010ù\u0001\u001a\u00030ú\u0001H\u0002J\u0007\u0010§\u0002\u001a\u00020\u001bJ\t\u0010¨\u0002\u001a\u00020\u001bH\u0014J\u0013\u0010©\u0002\u001a\u00020\u001b2\n\u0010ª\u0002\u001a\u0005\u0018\u00010«\u0002J\u0010\u0010¬\u0002\u001a\u00020\u001b2\u0007\u0010¬\u0001\u001a\u00020\rJ\u0011\u0010\u00ad\u0002\u001a\u00020\u001b2\b\u0010®\u0002\u001a\u00030À\u0001J\u0010\u0010¯\u0002\u001a\u00020\u001b2\u0007\u0010\u0098\u0001\u001a\u00020\rJ\u0007\u0010°\u0002\u001a\u00020\u001bJ\u0007\u0010±\u0002\u001a\u00020\rJ\u0007\u0010²\u0002\u001a\u00020\u001bJ\u0007\u0010³\u0002\u001a\u00020\u001bR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u0004¢\u0006\u0002\n\u0000R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0082\u0004¢\u0006\u0002\n\u0000R3\u0010\u0018\u001a%\b\u0001\u0012\u0004\u0012\u00020\u0000\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001b0\u001a\u0012\u0006\u0012\u0004\u0018\u00010\u001c\u0018\u00010\u0019¢\u0006\u0002\b\u001dX\u0082\u0004¢\u0006\u0004\n\u0002\u0010#R\u0010\u0010\u001e\u001a\u0004\u0018\u00010\u001fX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010$\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020&X\u0082\u0004¢\u0006\u0002\n\u0000R\u0018\u0010'\u001a\n )*\u0004\u0018\u00010(0(X\u0082\u0004¢\u0006\u0004\n\u0002\u0010*R\u000e\u0010+\u001a\u00020,X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010-\u001a\u00020\rX\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010.\u001a\u00020/X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u00100\u001a\u000201X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u00102\u001a\u000203X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u00104\u001a\u000205X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u00106\u001a\u000207X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u00108\u001a\u000209X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010:\u001a\u00020;X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010<\u001a\u00020=X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010>\u001a\u00020?X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010@\u001a\u00020AX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010B\u001a\u00020CX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010D\u001a\u00020EX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010F\u001a\u00020GX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010H\u001a\u00020IX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010J\u001a\u00020KX\u0082\u0004¢\u0006\u0002\n\u0000R\u0010\u0010L\u001a\u0004\u0018\u00010MX\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010N\u001a\u0004\u0018\u00010MX\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010O\u001a\u0004\u0018\u00010MX\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010P\u001a\u0004\u0018\u00010MX\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010Q\u001a\u0004\u0018\u00010RX\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010S\u001a\u0004\u0018\u00010MX\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010T\u001a\u0004\u0018\u00010MX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010U\u001a\u00020\rX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010V\u001a\u00020\rX\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010W\u001a\u0004\u0018\u00010RX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010X\u001a\u00020YX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010Z\u001a\u00020[X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\\\u001a\u00020\rX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010]\u001a\u00020^X\u0082\u0004¢\u0006\u0002\n\u0000R\u0014\u0010_\u001a\b\u0012\u0004\u0012\u00020a0`X\u0082\u0004¢\u0006\u0002\n\u0000R\u0017\u0010b\u001a\b\u0012\u0004\u0012\u00020a0c¢\u0006\b\n\u0000\u001a\u0004\bd\u0010eR\u0014\u0010f\u001a\b\u0012\u0004\u0012\u00020h0gX\u0082\u0004¢\u0006\u0002\n\u0000R\u0017\u0010i\u001a\b\u0012\u0004\u0012\u00020h0j¢\u0006\b\n\u0000\u001a\u0004\bk\u0010lR\u001b\u0010m\u001a\u00020n8BX\u0082\u0084\u0002¢\u0006\f\n\u0004\bq\u0010r\u001a\u0004\bo\u0010pR\u0014\u0010s\u001a\b\u0012\u0004\u0012\u00020u0tX\u0082\u0004¢\u0006\u0002\n\u0000R\u0014\u0010v\u001a\u00020u8BX\u0082\u0004¢\u0006\u0006\u001a\u0004\bw\u0010xR\u001b\u0010y\u001a\u00020z8BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b}\u0010r\u001a\u0004\b{\u0010|¨\u0006¶\u0002"},
   d2 = {"Lcom/example/teamcompass/ui/TeamCompassViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "app", "Landroid/app/Application;", "teamRepository", "Lcom/example/teamcompass/domain/TeamRepository;", "trackingController", "Lcom/example/teamcompass/domain/TrackingController;", "prefs", "Lcom/example/teamcompass/ui/UserPrefs;", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "autoStart", "", "actionTraceIdProvider", "Lcom/example/teamcompass/ui/ActionTraceIdProvider;", "structuredLogger", "Lcom/example/teamcompass/ui/StructuredLogger;", "coroutineExceptionHandler", "Lkotlinx/coroutines/CoroutineExceptionHandler;", "identityLinkingService", "Lcom/example/teamcompass/auth/IdentityLinkingService;", "p2pTransportManager", "Lcom/example/teamcompass/p2p/P2PTransportManager;", "initializeAutoStartOverride", "Lkotlin/Function2;", "Lkotlin/coroutines/Continuation;", "", "", "Lkotlin/ExtensionFunctionType;", "savedStateHandle", "Landroidx/lifecycle/SavedStateHandle;", "<init>", "(Landroid/app/Application;Lcom/example/teamcompass/domain/TeamRepository;Lcom/example/teamcompass/domain/TrackingController;Lcom/example/teamcompass/ui/UserPrefs;Lcom/google/firebase/auth/FirebaseAuth;ZLcom/example/teamcompass/ui/ActionTraceIdProvider;Lcom/example/teamcompass/ui/StructuredLogger;Lkotlinx/coroutines/CoroutineExceptionHandler;Lcom/example/teamcompass/auth/IdentityLinkingService;Lcom/example/teamcompass/p2p/P2PTransportManager;Lkotlin/jvm/functions/Function2;Landroidx/lifecycle/SavedStateHandle;)V", "(Landroid/app/Application;Lcom/example/teamcompass/domain/TeamRepository;Lcom/example/teamcompass/domain/TrackingController;Lcom/example/teamcompass/ui/UserPrefs;Lcom/google/firebase/auth/FirebaseAuth;Lkotlinx/coroutines/CoroutineExceptionHandler;Lcom/example/teamcompass/p2p/P2PTransportManager;Landroidx/lifecycle/SavedStateHandle;)V", "Lkotlin/jvm/functions/Function2;", "application", "fusedPreview", "Lcom/google/android/gms/location/FusedLocationProviderClient;", "vibrator", "Landroid/os/Vibrator;", "kotlin.jvm.PlatformType", "Landroid/os/Vibrator;", "tone", "Landroid/media/ToneGenerator;", "tacticalFiltersEnabled", "locationReadinessCoordinator", "Lcom/example/teamcompass/ui/LocationReadinessCoordinator;", "targetFilterCoordinator", "Lcom/example/teamcompass/ui/TargetFilterCoordinator;", "authDelegate", "Lcom/example/teamcompass/ui/AuthDelegate;", "teamSessionDelegate", "Lcom/example/teamcompass/ui/TeamSessionDelegate;", "trackingCoordinator", "Lcom/example/teamcompass/ui/TrackingCoordinator;", "alertsCoordinator", "Lcom/example/teamcompass/ui/AlertsCoordinator;", "mapCoordinator", "Lcom/example/teamcompass/ui/MapCoordinator;", "joinRateLimiter", "Lcom/example/teamcompass/ui/JoinRateLimiter;", "backendHealthMonitor", "Lcom/example/teamcompass/ui/BackendHealthMonitor;", "backendHealthDelegate", "Lcom/example/teamcompass/ui/BackendHealthDelegate;", "teamSnapshotObserver", "Lcom/example/teamcompass/ui/TeamSnapshotObserver;", "memberPrefsSyncWorker", "Lcom/example/teamcompass/ui/MemberPrefsSyncWorker;", "eventNotificationManager", "Lcom/example/teamcompass/ui/EventNotificationManager;", "analytics", "Lcom/google/firebase/analytics/FirebaseAnalytics;", "autoBrightnessBinding", "Lcom/example/teamcompass/ui/AutoBrightnessBinding;", "teamObserverJob", "Lkotlinx/coroutines/Job;", "memberPrefsObserverJob", "memberPrefsSyncJob", "p2pObserverJob", "identityLinkingPromptTrackedUid", "", "deadReminderJob", "locationServiceMonitorJob", "targetFilterDirtyByUser", "lastBackendHealthAvailableSample", "restoredTeamCode", "restoredDefaultMode", "Lcom/example/teamcompass/core/TrackingMode;", "restoredPlayerMode", "Lcom/example/teamcompass/core/PlayerMode;", "restoredIsTracking", "restoredMySosUntilMs", "", "_ui", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/teamcompass/ui/UiState;", "ui", "Lkotlinx/coroutines/flow/StateFlow;", "getUi", "()Lkotlinx/coroutines/flow/StateFlow;", "_events", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/example/teamcompass/ui/UiEvent;", "events", "Lkotlinx/coroutines/flow/SharedFlow;", "getEvents", "()Lkotlinx/coroutines/flow/SharedFlow;", "headingSensorCoordinator", "Lcom/example/teamcompass/ui/HeadingSensorCoordinator;", "getHeadingSensorCoordinator", "()Lcom/example/teamcompass/ui/HeadingSensorCoordinator;", "headingSensorCoordinator$delegate", "Lkotlin/Lazy;", "bluetoothScanCoordinatorLazy", "Lkotlin/Lazy;", "Lcom/example/teamcompass/ui/BluetoothScanCoordinator;", "bluetoothScanCoordinator", "getBluetoothScanCoordinator", "()Lcom/example/teamcompass/ui/BluetoothScanCoordinator;", "tacticalActionsCoordinator", "Lcom/example/teamcompass/ui/TacticalActionsCoordinator;", "getTacticalActionsCoordinator", "()Lcom/example/teamcompass/ui/TacticalActionsCoordinator;", "tacticalActionsCoordinator$delegate", "newTraceId", "action", "logActionStart", "traceId", "teamCode", "uid", "logActionSuccess", "logActionFailure", "throwable", "", "message", "initializeAutoStart", "setUiForTest", "transform", "Lkotlin/Function1;", "setUiForTest$app_debug", "refreshBackendStaleFlagForTest", "nowMs", "refreshBackendStaleFlagForTest$app_debug", "emitError", "cause", "bindPrefs", "bindTrackingController", "ensureAuth", "onAuthReady", "setCallsign", "value", "setDefaultMode", "mode", "setTeamViewMode", "Lcom/example/teamcompass/domain/TeamViewMode;", "setGamePolicy", "intervalSec", "", "distanceM", "setSilentPolicy", "setThemeMode", "Lcom/example/teamcompass/ui/theme/ThemeMode;", "setLocationPermission", "granted", "refreshLocationReadiness", "bindSavedStateHandle", "refreshLocationPreview", "togglePlayerMode", "setPlayerMode", "setEnemyMarkEnabled", "enabled", "setTargetPreset", "preset", "Lcom/example/teamcompass/core/TargetFilterPreset;", "setNearRadius", "radiusM", "setShowDead", "showDead", "setShowStale", "showStale", "setFocusMode", "updateTargetFilterStateByUser", "Lcom/example/teamcompass/core/TargetFilterState;", "importTacticalMap", "uri", "Landroid/net/Uri;", "clearTacticalMap", "setMapEnabled", "setMapOpacity", "opacity", "", "saveMapChangesToSource", "newPoints", "", "Lcom/example/teamcompass/ui/KmlPoint;", "deletedPoints", "saveMapChangesAs", "saveMapChangesInternal", "destinationUri", "map", "Lcom/example/teamcompass/ui/TacticalMap;", "toggleSos", "triggerSos", "clearSos", "addPointHere", "label", "icon", "forTeam", "addPointAt", "lat", "", "lon", "updatePoint", "id", "isTeam", "deletePoint", "sendQuickCommand", "type", "Lcom/example/teamcompass/ui/QuickCommandType;", "addEnemyPing", "assignTeamMemberRole", "targetUid", "patch", "Lcom/example/teamcompass/domain/TeamRolePatch;", "assignTeamMemberRolesBulk", "targetUids", "createTeam", "joinTeam", "codeRaw", "alsoCreateMember", "onTeamJoined", "code", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "evaluateIdentityLinkingEligibility", "markCompassHelpSeen", "markOnboardingSeen", "setControlLayoutEdit", "setControlPosition", "Lcom/example/teamcompass/ui/CompassControlId;", "position", "Lcom/example/teamcompass/ui/ControlPosition;", "resetControlPositions", "applyControlLayoutPreset", "Lcom/example/teamcompass/ui/ControlLayoutPreset;", "leaveTeam", "startListening", "handleStartListeningTerminalFailure", "failure", "Lcom/example/teamcompass/domain/TeamActionFailure;", "clearTeamSessionStateForTerminalFailure", "collectTeamSnapshotsWithReconnect", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "stopListening", "startP2PInboundObservation", "localUid", "handleP2PInbound", "inbound", "Lcom/example/teamcompass/p2p/P2PInboundMessage;", "startBackendHealthMonitor", "startBackendStaleMonitor", "scheduleBackendStaleRefresh", "refreshBackendStaleFlag", "computeBackendStale", "lastSnapshotAtMs", "startMemberPrefsSync", "processEnemyPingAlerts", "enemyPings", "Lcom/example/teamcompass/ui/EnemyPing;", "processSosAlerts", "vibrateAndBeep", "strong", "startDeadReminder", "stopDeadReminder", "startTracking", "persistMode", "restartTracking", "stopTracking", "dismissError", "computeTargets", "Lcom/example/teamcompass/core/CompassTarget;", "refreshTargetsFromState", "startHeading", "stopHeading", "startLocationServiceMonitor", "normalizeTeamCode", "raw", "tr", "resId", "args", "", "(I[Ljava/lang/Object;)Ljava/lang/String;", "handleActionFailure", "defaultMessage", "logPerfMetricsSnapshot", "onCleared", "bindAutoBrightnessWindow", "window", "Landroid/view/Window;", "setAutoBrightnessEnabled", "setScreenBrightness", "brightness", "setHasStartedOnce", "autoStartTrackingIfNeeded", "hasBluetoothPermission", "startBluetoothScan", "cancelBluetoothScan", "Companion", "RestorableVmState", "app_debug"}
)
@HiltViewModel
@StabilityInferred(
   parameters = 0
)
@SourceDebugExtension({"SMAP\nTeamCompassViewModel.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TeamCompassViewModel.kt\ncom/example/teamcompass/ui/TeamCompassViewModel\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 3 CoroutineExceptionHandler.kt\nkotlinx/coroutines/CoroutineExceptionHandlerKt\n+ 4 StateFlow.kt\nkotlinx/coroutines/flow/StateFlowKt\n+ 5 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 6 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt\n+ 7 SafeCollector.common.kt\nkotlinx/coroutines/flow/internal/SafeCollector_commonKt\n+ 8 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,1920:1\n1#2:1921\n46#3,4:1922\n230#4,5:1926\n230#4,5:1931\n230#4,5:1936\n230#4,5:1941\n230#4,5:1946\n230#4,5:1951\n230#4,5:1956\n230#4,5:1961\n230#4,5:1966\n230#4,5:1971\n230#4,5:1976\n230#4,5:1981\n230#4,5:1986\n230#4,5:1991\n230#4,5:1996\n230#4,5:2001\n230#4,5:2006\n230#4,5:2011\n230#4,5:2016\n230#4,5:2021\n230#4,5:2026\n230#4,5:2031\n230#4,5:2036\n230#4,5:2041\n230#4,5:2046\n230#4,5:2051\n230#4,5:2056\n230#4,5:2061\n230#4,5:2066\n230#4,5:2071\n230#4,5:2076\n230#4,5:2081\n230#4,5:2086\n230#4,5:2091\n230#4,5:2096\n230#4,5:2101\n230#4,5:2106\n230#4,5:2111\n230#4,5:2116\n230#4,5:2131\n230#4,5:2136\n230#4,5:2141\n230#4,5:2146\n230#4,5:2151\n230#4,5:2156\n230#4,5:2161\n230#4,5:2166\n230#4,5:2171\n230#4,5:2176\n230#4,5:2181\n230#4,5:2186\n230#4,5:2191\n230#4,5:2196\n230#4,5:2201\n49#5:2121\n51#5:2125\n46#6:2122\n51#6:2124\n105#7:2123\n827#8:2126\n855#8,2:2127\n1863#8,2:2129\n*S KotlinDebug\n*F\n+ 1 TeamCompassViewModel.kt\ncom/example/teamcompass/ui/TeamCompassViewModel\n*L\n77#1:1922,4\n329#1:1926,5\n336#1:1931,5\n346#1:1936,5\n522#1:1941,5\n528#1:1946,5\n534#1:1951,5\n542#1:1956,5\n552#1:1961,5\n562#1:1966,5\n570#1:1971,5\n575#1:1976,5\n576#1:1981,5\n589#1:1986,5\n630#1:1991,5\n664#1:1996,5\n675#1:2001,5\n704#1:2006,5\n715#1:2011,5\n733#1:2016,5\n737#1:2021,5\n741#1:2026,5\n794#1:2031,5\n838#1:2036,5\n843#1:2041,5\n979#1:2046,5\n1021#1:2051,5\n1061#1:2056,5\n1108#1:2061,5\n1113#1:2066,5\n1118#1:2071,5\n1124#1:2076,5\n1130#1:2081,5\n1136#1:2086,5\n1149#1:2091,5\n1275#1:2096,5\n1389#1:2101,5\n1438#1:2106,5\n1518#1:2111,5\n1551#1:2116,5\n1673#1:2131,5\n1676#1:2136,5\n1692#1:2141,5\n1730#1:2146,5\n1734#1:2151,5\n1741#1:2156,5\n1757#1:2161,5\n1862#1:2166,5\n219#1:2171,5\n229#1:2176,5\n241#1:2181,5\n509#1:2186,5\n637#1:2191,5\n1582#1:2196,5\n1780#1:2201,5\n1578#1:2121\n1578#1:2125\n1578#1:2122\n1578#1:2124\n1578#1:2123\n1603#1:2126\n1603#1:2127,2\n1622#1:2129,2\n*E\n"})
public final class TeamCompassViewModel extends AndroidViewModel {
   @NotNull
   public static final Companion Companion = new Companion((DefaultConstructorMarker)null);
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
   private final Function2 initializeAutoStartOverride;
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
   private final MutableStateFlow _ui;
   @NotNull
   private final StateFlow ui;
   @NotNull
   private final MutableSharedFlow _events;
   @NotNull
   private final SharedFlow events;
   @NotNull
   private final Lazy headingSensorCoordinator$delegate;
   @NotNull
   private final Lazy bluetoothScanCoordinatorLazy;
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

   public TeamCompassViewModel(@NotNull Application app, @NotNull TeamRepository teamRepository, @NotNull TrackingController trackingController, @NotNull UserPrefs prefs, @NotNull FirebaseAuth auth, boolean autoStart, @NotNull ActionTraceIdProvider actionTraceIdProvider, @NotNull StructuredLogger structuredLogger, @NotNull CoroutineExceptionHandler coroutineExceptionHandler, @NotNull IdentityLinkingService identityLinkingService, @Nullable P2PTransportManager p2pTransportManager, @Nullable Function2 initializeAutoStartOverride, @Nullable SavedStateHandle savedStateHandle) {
      String var40;
      TeamCompassViewModel var10000;
      label66: {
         Intrinsics.checkNotNullParameter(app, "app");
         Intrinsics.checkNotNullParameter(teamRepository, "teamRepository");
         Intrinsics.checkNotNullParameter(trackingController, "trackingController");
         Intrinsics.checkNotNullParameter(prefs, "prefs");
         Intrinsics.checkNotNullParameter(auth, "auth");
         Intrinsics.checkNotNullParameter(actionTraceIdProvider, "actionTraceIdProvider");
         Intrinsics.checkNotNullParameter(structuredLogger, "structuredLogger");
         Intrinsics.checkNotNullParameter(coroutineExceptionHandler, "coroutineExceptionHandler");
         Intrinsics.checkNotNullParameter(identityLinkingService, "identityLinkingService");
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
         FusedLocationProviderClient var10001 = LocationServices.getFusedLocationProviderClient((Context)this.application);
         Intrinsics.checkNotNullExpressionValue(var10001, "getFusedLocationProviderClient(...)");
         this.fusedPreview = var10001;
         this.vibrator = (Vibrator)this.application.getSystemService(Vibrator.class);
         this.tone = new ToneGenerator(4, 100);
         this.tacticalFiltersEnabled = true;
         this.locationReadinessCoordinator = new LocationReadinessCoordinator(this.application);
         this.targetFilterCoordinator = new TargetFilterCoordinator(this.tacticalFiltersEnabled, (CompassCalculator)null, 2, (DefaultConstructorMarker)null);
         this.authDelegate = new AuthDelegate((AuthGateway)(new FirebaseAuthGateway(this.auth)));
         this.teamSessionDelegate = new TeamSessionDelegate(new SessionCoordinator(this.teamRepository, 15000L, (Function0)null, 4, (DefaultConstructorMarker)null), this.p2pTransportManager);
         this.trackingCoordinator = new TrackingCoordinator(this.trackingController, (String)null, 2, (DefaultConstructorMarker)null);
         this.alertsCoordinator = new AlertsCoordinator(0L, (double)0.0F, 3, (DefaultConstructorMarker)null);
         this.mapCoordinator = new MapCoordinator((Function3)null, (Function6)null, 3, (DefaultConstructorMarker)null);
         this.joinRateLimiter = new JoinRateLimiter((Function0)null, 0L, 0, 0, 15, (DefaultConstructorMarker)null);
         this.backendHealthMonitor = new BackendHealthMonitor(this.teamRepository);
         this.backendHealthDelegate = new BackendHealthDelegate(this.backendHealthMonitor, 30000L, (Function0)null, (Function2)null, 12, (DefaultConstructorMarker)null);
         this.teamSnapshotObserver = new TeamSnapshotObserver(this.teamRepository, 1500L, 20000L, (Function2)null, 8, (DefaultConstructorMarker)null);
         this.memberPrefsSyncWorker = new MemberPrefsSyncWorker(this.teamRepository, this.tacticalFiltersEnabled);
         this.eventNotificationManager = new EventNotificationManager(this.application);
         FirebaseAnalytics var39 = FirebaseAnalytics.getInstance((Context)this.application);
         Intrinsics.checkNotNullExpressionValue(var39, "getInstance(...)");
         this.analytics = var39;
         this.autoBrightnessBinding = new AutoBrightnessBinding(this.application, TeamCompassViewModel::autoBrightnessBinding$lambda$1);
         this.lastBackendHealthAvailableSample = true;
         var10000 = this;
         SavedStateHandle var14 = this.savedStateHandle;
         if (var14 != null) {
            String var15 = (String)var14.get("state_team_code");
            if (var15 != null) {
               String var16 = StringsKt.trim((CharSequence)var15).toString();
               if (var16 != null) {
                  int var19 = 0;
                  boolean var23 = ((CharSequence)var16).length() > 0;
                  var10000 = this;
                  var40 = var23 ? var16 : null;
                  break label66;
               }
            }
         }

         var40 = null;
      }

      label60: {
         var10000.restoredTeamCode = var40;
         var10000 = this;
         SavedStateHandle var24 = this.savedStateHandle;
         if (var24 != null) {
            String var28 = (String)var24.get("state_default_mode");
            if (var28 != null) {
               Companion it = Companion;
               int $i$f$restoredDefaultMode$stub_for_inlining = 0;
               int var21 = 0;
               TrackingMode var41 = com.example.teamcompass.ui.TeamCompassViewModel.Companion.access$parseTrackingModeOrNull(it, var28);
               var10000 = this;
               TrackingMode var32 = var41;
               if (var32 != null) {
                  var42 = var32;
                  break label60;
               }
            }
         }

         var42 = TrackingMode.GAME;
      }

      label54: {
         var10000.restoredDefaultMode = var42;
         var10000 = this;
         SavedStateHandle var25 = this.savedStateHandle;
         if (var25 != null) {
            String var29 = (String)var25.get("state_player_mode");
            if (var29 != null) {
               Companion var34 = Companion;
               int $i$f$restoredPlayerMode$stub_for_inlining$3 = 0;
               int var36 = 0;
               PlayerMode var43 = com.example.teamcompass.ui.TeamCompassViewModel.Companion.access$parsePlayerModeOrNull(var34, var29);
               var10000 = this;
               PlayerMode var33 = var43;
               if (var33 != null) {
                  var44 = var33;
                  break label54;
               }
            }
         }

         var44 = PlayerMode.GAME;
      }

      label48: {
         var10000.restoredPlayerMode = var44;
         SavedStateHandle var26 = this.savedStateHandle;
         if (var26 != null) {
            Boolean var30 = (Boolean)var26.get("state_is_tracking");
            if (var30 != null) {
               var45 = var30;
               break label48;
            }
         }

         var45 = false;
      }

      label43: {
         this.restoredIsTracking = var45;
         SavedStateHandle var27 = this.savedStateHandle;
         if (var27 != null) {
            Long var31 = (Long)var27.get("state_my_sos_until_ms");
            if (var31 != null) {
               var46 = var31;
               break label43;
            }
         }

         var46 = 0L;
      }

      this.restoredMySosUntilMs = var46;
      this._ui = StateFlowKt.MutableStateFlow(new UiState((AuthState)null, new TrackingUiState(this.restoredIsTracking, this.locationReadinessCoordinator.hasLocationPermission(), this.locationReadinessCoordinator.isLocationServiceEnabled(), (LocationPoint)null, (Double)null, this.restoredDefaultMode, false, false, (TelemetryState)null, 472, (DefaultConstructorMarker)null), new TeamUiState((String)null, this.restoredTeamCode, (List)null, (List)null, this.restoredPlayerMode, this.restoredMySosUntilMs, (QuickCommand)null, (TeamViewMode)null, false, 461, (DefaultConstructorMarker)null), (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 249, (DefaultConstructorMarker)null));
      this.ui = FlowKt.asStateFlow(this._ui);
      this._events = SharedFlowKt.MutableSharedFlow$default(0, 32, (BufferOverflow)null, 5, (Object)null);
      this.events = FlowKt.asSharedFlow(this._events);
      this.headingSensorCoordinator$delegate = LazyKt.lazy(TeamCompassViewModel::headingSensorCoordinator_delegate$lambda$6);
      this.bluetoothScanCoordinatorLazy = LazyKt.lazy(TeamCompassViewModel::bluetoothScanCoordinatorLazy$lambda$10);
      this.tacticalActionsCoordinator$delegate = LazyKt.lazy(TeamCompassViewModel::tacticalActionsCoordinator_delegate$lambda$14);
      this.bindSavedStateHandle();
      if (this.autoStart) {
         BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new 2(this, (Continuation)null)), 2, (Object)null);
      }

   }

   // $FF: synthetic method
   public TeamCompassViewModel(Application var1, TeamRepository var2, TrackingController var3, UserPrefs var4, FirebaseAuth var5, boolean var6, ActionTraceIdProvider var7, StructuredLogger var8, CoroutineExceptionHandler var9, IdentityLinkingService var10, P2PTransportManager var11, Function2 var12, SavedStateHandle var13, int var14, DefaultConstructorMarker var15) {
      if ((var14 & 16) != 0) {
         var5 = FirebaseAuth.getInstance();
      }

      if ((var14 & 32) != 0) {
         var6 = true;
      }

      if ((var14 & 64) != 0) {
         var7 = (ActionTraceIdProvider)(new UuidActionTraceIdProvider());
      }

      if ((var14 & 128) != 0) {
         var8 = (StructuredLogger)(new CrashlyticsStructuredLogger((String)null, (Function0)null, 3, (DefaultConstructorMarker)null));
      }

      if ((var14 & 256) != 0) {
         int $i$f$CoroutineExceptionHandler = 0;
         CoroutineExceptionHandler.Key var17 = CoroutineExceptionHandler.Key;
         var9 = (CoroutineExceptionHandler)(new special..inlined.CoroutineExceptionHandler.1(var17));
      }

      if ((var14 & 512) != 0) {
         var10 = (IdentityLinkingService)NoOpIdentityLinkingService.INSTANCE;
      }

      if ((var14 & 1024) != 0) {
         var11 = null;
      }

      if ((var14 & 2048) != 0) {
         var12 = null;
      }

      if ((var14 & 4096) != 0) {
         var13 = null;
      }

      this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var13);
   }

   @Inject
   public TeamCompassViewModel(@NotNull Application app, @NotNull TeamRepository teamRepository, @NotNull TrackingController trackingController, @NotNull UserPrefs prefs, @NotNull FirebaseAuth auth, @NotNull CoroutineExceptionHandler coroutineExceptionHandler, @NotNull P2PTransportManager p2pTransportManager, @NotNull SavedStateHandle savedStateHandle) {
      Intrinsics.checkNotNullParameter(app, "app");
      Intrinsics.checkNotNullParameter(teamRepository, "teamRepository");
      Intrinsics.checkNotNullParameter(trackingController, "trackingController");
      Intrinsics.checkNotNullParameter(prefs, "prefs");
      Intrinsics.checkNotNullParameter(auth, "auth");
      Intrinsics.checkNotNullParameter(coroutineExceptionHandler, "coroutineExceptionHandler");
      Intrinsics.checkNotNullParameter(p2pTransportManager, "p2pTransportManager");
      Intrinsics.checkNotNullParameter(savedStateHandle, "savedStateHandle");
      this(app, teamRepository, trackingController, prefs, auth, true, (ActionTraceIdProvider)(new UuidActionTraceIdProvider()), (StructuredLogger)(new CrashlyticsStructuredLogger((String)null, (Function0)null, 3, (DefaultConstructorMarker)null)), coroutineExceptionHandler, (IdentityLinkingService)(new FirebaseIdentityLinkingService(auth)), p2pTransportManager, (Function2)null, savedStateHandle);
   }

   @NotNull
   public final StateFlow getUi() {
      return this.ui;
   }

   @NotNull
   public final SharedFlow getEvents() {
      return this.events;
   }

   private final HeadingSensorCoordinator getHeadingSensorCoordinator() {
      Lazy var1 = this.headingSensorCoordinator$delegate;
      return (HeadingSensorCoordinator)var1.getValue();
   }

   private final BluetoothScanCoordinator getBluetoothScanCoordinator() {
      return (BluetoothScanCoordinator)this.bluetoothScanCoordinatorLazy.getValue();
   }

   private final TacticalActionsCoordinator getTacticalActionsCoordinator() {
      Lazy var1 = this.tacticalActionsCoordinator$delegate;
      return (TacticalActionsCoordinator)var1.getValue();
   }

   private final String newTraceId(String action) {
      return this.actionTraceIdProvider.nextTraceId(action);
   }

   private final void logActionStart(String action, String traceId, String teamCode, String uid) {
      this.structuredLogger.logStart(action, traceId, teamCode, uid, ((UiState)this._ui.getValue()).getTelemetry().getBackendAvailable());
   }

   // $FF: synthetic method
   static void logActionStart$default(TeamCompassViewModel var0, String var1, String var2, String var3, String var4, int var5, Object var6) {
      if ((var5 & 4) != 0) {
         var3 = ((UiState)var0._ui.getValue()).getTeamCode();
      }

      if ((var5 & 8) != 0) {
         var4 = ((UiState)var0._ui.getValue()).getUid();
      }

      var0.logActionStart(var1, var2, var3, var4);
   }

   private final void logActionSuccess(String action, String traceId, String teamCode, String uid) {
      this.structuredLogger.logSuccess(action, traceId, teamCode, uid, ((UiState)this._ui.getValue()).getTelemetry().getBackendAvailable());
   }

   // $FF: synthetic method
   static void logActionSuccess$default(TeamCompassViewModel var0, String var1, String var2, String var3, String var4, int var5, Object var6) {
      if ((var5 & 4) != 0) {
         var3 = ((UiState)var0._ui.getValue()).getTeamCode();
      }

      if ((var5 & 8) != 0) {
         var4 = ((UiState)var0._ui.getValue()).getUid();
      }

      var0.logActionSuccess(var1, var2, var3, var4);
   }

   private final void logActionFailure(String action, String traceId, Throwable throwable, String message, String teamCode, String uid) {
      this.structuredLogger.logFailure(action, traceId, teamCode, uid, ((UiState)this._ui.getValue()).getTelemetry().getBackendAvailable(), throwable, message);
   }

   // $FF: synthetic method
   static void logActionFailure$default(TeamCompassViewModel var0, String var1, String var2, Throwable var3, String var4, String var5, String var6, int var7, Object var8) {
      if ((var7 & 4) != 0) {
         var3 = null;
      }

      if ((var7 & 8) != 0) {
         var4 = null;
      }

      if ((var7 & 16) != 0) {
         var5 = ((UiState)var0._ui.getValue()).getTeamCode();
      }

      if ((var7 & 32) != 0) {
         var6 = ((UiState)var0._ui.getValue()).getUid();
      }

      var0.logActionFailure(var1, var2, var3, var4, var5, var6);
   }

   private final void initializeAutoStart() {
      this.bindPrefs();
      this.bindTrackingController();
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState state = (UiState)prevValue$iv;
         int var5 = 0;
         nextValue$iv = this.locationReadinessCoordinator.applyServiceState(state);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      this.startLocationServiceMonitor();
      this.ensureAuth();
   }

   @VisibleForTesting
   public final void setUiForTest$app_debug(@NotNull Function1 transform) {
      Intrinsics.checkNotNullParameter(transform, "transform");
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         nextValue$iv = transform.invoke(prevValue$iv);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

   }

   @VisibleForTesting
   public final void refreshBackendStaleFlagForTest$app_debug(long nowMs) {
      this.refreshBackendStaleFlag(nowMs);
   }

   public final void emitError(@NotNull String message, @Nullable Throwable cause) {
      Intrinsics.checkNotNullParameter(message, "message");
      if (cause != null) {
         Log.w("TeamCompassVM", message, cause);
      } else {
         Log.w("TeamCompassVM", message);
      }

      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var7 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, message, 127, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      this._events.tryEmit(new UiEvent.Error(message));
   }

   // $FF: synthetic method
   public static void emitError$default(TeamCompassViewModel var0, String var1, Throwable var2, int var3, Object var4) {
      if ((var3 & 2) != 0) {
         var2 = null;
      }

      var0.emitError(var1, var2);
   }

   private final void bindPrefs() {
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindPrefs.1(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindPrefs.2(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindPrefs.3(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindPrefs.4(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindPrefs.5(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindPrefs.6(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindPrefs.7(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindPrefs.8(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindPrefs.9(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindPrefs.10(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindPrefs.11(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindPrefs.12(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindPrefs.13(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindPrefs.14(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindPrefs.15(this, (Continuation)null)), 2, (Object)null);
   }

   private final void bindTrackingController() {
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindTrackingController.1(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindTrackingController.2(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindTrackingController.3(this, (Continuation)null)), 2, (Object)null);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindTrackingController.4(this, (Continuation)null)), 2, (Object)null);
   }

   public final void ensureAuth() {
      String traceId = this.newTraceId("ensureAuth");
      logActionStart$default(this, "ensureAuth", traceId, (String)null, (String)null, 12, (Object)null);
      this.authDelegate.ensureAuth(TeamCompassViewModel::ensureAuth$lambda$17, TeamCompassViewModel::ensureAuth$lambda$19);
   }

   private final void onAuthReady(String uid) {
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var6 = 0;
         nextValue$iv = UiState.copy$default(it, it.getAuth().copy(true, uid), (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 254, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      String var10000 = ((UiState)this._ui.getValue()).getTeamCode();
      if (var10000 != null) {
         String it = var10000;
         int var9 = 0;
         this.startListening(it);
      }

   }

   public final void setCallsign(@NotNull String value) {
      Intrinsics.checkNotNullParameter(value, "value");
      String callsign = StringsKt.take(value, 24);
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var7 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, TeamUiState.copy$default(it.getTeam(), callsign, (String)null, (List)null, (List)null, (PlayerMode)null, 0L, (QuickCommand)null, (TeamViewMode)null, false, 510, (Object)null), (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 251, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new setCallsign.2(this, callsign, (Continuation)null)), 2, (Object)null);
   }

   public final void setDefaultMode(@NotNull TrackingMode mode) {
      Intrinsics.checkNotNullParameter(mode, "mode");
      boolean wasTracking = ((UiState)this._ui.getValue()).isTracking();
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var7 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, TrackingUiState.copy$default(it.getTracking(), false, false, false, (LocationPoint)null, (Double)null, mode, false, false, (TelemetryState)null, 479, (Object)null), (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 253, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new setDefaultMode.2(this, mode, (Continuation)null)), 2, (Object)null);
      if (wasTracking) {
         this.restartTracking();
      }

   }

   public final void setTeamViewMode(@NotNull TeamViewMode mode) {
      Intrinsics.checkNotNullParameter(mode, "mode");
      TeamViewMode previous = ((UiState)this._ui.getValue()).getViewMode();
      if (previous != mode) {
         MutableStateFlow $this$update$iv = this._ui;
         int $i$f$update = 0;

         Object prevValue$iv;
         Object nextValue$iv;
         do {
            prevValue$iv = $this$update$iv.getValue();
            UiState it = (UiState)prevValue$iv;
            int var7 = 0;
            nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, TeamUiState.copy$default(it.getTeam(), (String)null, (String)null, (List)null, (List)null, (PlayerMode)null, 0L, (QuickCommand)null, mode, false, 383, (Object)null), (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 251, (Object)null);
         } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

         String teamCode = ((UiState)this._ui.getValue()).getTeamCode();
         CharSequence var9 = (CharSequence)teamCode;
         if (var9 != null && !StringsKt.isBlank(var9)) {
            this.startListening(teamCode);
         }

      }
   }

   public final void setGamePolicy(int intervalSec, int distanceM) {
      int safeInterval = RangesKt.coerceIn(intervalSec, 3, 20);
      int safeDistance = RangesKt.coerceIn(distanceM, 1, 100);
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var9 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, (FilterUiState)null, SettingsUiState.copy$default(it.getSettings(), safeInterval, safeDistance, 0, 0, false, 0.0F, (ThemeMode)null, false, (Map)null, false, false, 2044, (Object)null), (BluetoothUiState)null, (String)null, 223, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new setGamePolicy.2(this, safeInterval, safeDistance, (Continuation)null)), 2, (Object)null);
      if (((UiState)this._ui.getValue()).isTracking() && ((UiState)this._ui.getValue()).getDefaultMode() == TrackingMode.GAME) {
         this.restartTracking();
      }

   }

   public final void setSilentPolicy(int intervalSec, int distanceM) {
      int safeInterval = RangesKt.coerceIn(intervalSec, 10, 60);
      int safeDistance = RangesKt.coerceIn(distanceM, 1, 200);
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var9 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, (FilterUiState)null, SettingsUiState.copy$default(it.getSettings(), 0, 0, safeInterval, safeDistance, false, 0.0F, (ThemeMode)null, false, (Map)null, false, false, 2035, (Object)null), (BluetoothUiState)null, (String)null, 223, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new setSilentPolicy.2(this, safeInterval, safeDistance, (Continuation)null)), 2, (Object)null);
      if (((UiState)this._ui.getValue()).isTracking() && ((UiState)this._ui.getValue()).getDefaultMode() == TrackingMode.SILENT) {
         this.restartTracking();
      }

   }

   public final void setThemeMode(@NotNull ThemeMode mode) {
      Intrinsics.checkNotNullParameter(mode, "mode");
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var6 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, (FilterUiState)null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0F, mode, false, (Map)null, false, false, 1983, (Object)null), (BluetoothUiState)null, (String)null, 223, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new setThemeMode.2(this, mode, (Continuation)null)), 2, (Object)null);
   }

   public final void setLocationPermission(boolean granted) {
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var6 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, TrackingUiState.copy$default(it.getTracking(), false, granted, false, (LocationPoint)null, (Double)null, (TrackingMode)null, false, false, (TelemetryState)null, 509, (Object)null), (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 253, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      $this$update$iv = this._ui;
      $i$f$update = 0;

      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState state = (UiState)prevValue$iv;
         int var13 = 0;
         nextValue$iv = this.locationReadinessCoordinator.applyServiceState(state);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      if (!granted) {
         this.stopTracking();
      } else {
         this.refreshLocationPreview();
      }
   }

   public final void refreshLocationReadiness() {
      String permissionError = this.tr(string.vm_error_location_permission_required);
      String servicesDisabledError = this.tr(string.vm_error_location_services_disabled);
      String trackingDisabledError = this.tr(string.vm_error_location_disabled_during_tracking);
      boolean shouldRefreshPreview = false;
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState state = (UiState)prevValue$iv;
         int var9 = 0;
         LocationReadinessUpdate update = this.locationReadinessCoordinator.refreshReadiness(state, permissionError, servicesDisabledError, trackingDisabledError);
         shouldRefreshPreview = update.getShouldRefreshPreview();
         nextValue$iv = update.getUpdatedState();
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      if (shouldRefreshPreview) {
         this.refreshLocationPreview();
      }

   }

   private final void bindSavedStateHandle() {
      SavedStateHandle var10000 = this.savedStateHandle;
      if (var10000 != null) {
         SavedStateHandle handle = var10000;
         BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new bindSavedStateHandle.1(this, handle, (Continuation)null)), 2, (Object)null);
      }
   }

   public final void refreshLocationPreview() {
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState state = (UiState)prevValue$iv;
         int var5 = 0;
         nextValue$iv = this.locationReadinessCoordinator.applyServiceState(state);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      if (this.locationReadinessCoordinator.hasLocationPermission()) {
         this.fusedPreview.getLastLocation().addOnSuccessListener(TeamCompassViewModel::refreshLocationPreview$lambda$34).addOnFailureListener(TeamCompassViewModel::refreshLocationPreview$lambda$35);
      }
   }

   public final void togglePlayerMode() {
      PlayerMode next = ((UiState)this._ui.getValue()).getPlayerMode() == PlayerMode.GAME ? PlayerMode.DEAD : PlayerMode.GAME;
      this.setPlayerMode(next);
   }

   public final void setPlayerMode(@NotNull PlayerMode mode) {
      Intrinsics.checkNotNullParameter(mode, "mode");
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var6 = 0;
         TeamUiState var7 = TeamUiState.copy$default(it.getTeam(), (String)null, (String)null, (List)null, (List)null, mode, 0L, (QuickCommand)null, (TeamViewMode)null, false, 495, (Object)null);
         TrackingUiState var8 = TrackingUiState.copy$default(it.getTracking(), false, false, false, (LocationPoint)null, (Double)null, (TrackingMode)null, false, false, (TelemetryState)null, 383, (Object)null);
         nextValue$iv = UiState.copy$default(it, (AuthState)null, var8, var7, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 249, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      if (mode == PlayerMode.DEAD) {
         this.startDeadReminder();
      } else {
         this.stopDeadReminder();
      }

      this.trackingController.updateStatus(mode, ((UiState)this._ui.getValue()).getMySosUntilMs(), true);
   }

   public final void setEnemyMarkEnabled(boolean enabled) {
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var6 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, MapUiState.copy$default(it.getMap(), (List)null, (List)null, (List)null, (List)null, enabled, (QuickCommandType)null, (TacticalMap)null, false, 0.0F, 495, (Object)null), (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 247, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

   }

   public final void setTargetPreset(@NotNull TargetFilterPreset preset) {
      Intrinsics.checkNotNullParameter(preset, "preset");
      this.updateTargetFilterStateByUser(TeamCompassViewModel::setTargetPreset$lambda$38);
   }

   public final void setNearRadius(int radiusM) {
      int safeRadius = RangesKt.coerceIn(radiusM, 50, 500);
      this.updateTargetFilterStateByUser(TeamCompassViewModel::setNearRadius$lambda$39);
   }

   public final void setShowDead(boolean showDead) {
      this.updateTargetFilterStateByUser(TeamCompassViewModel::setShowDead$lambda$40);
   }

   public final void setShowStale(boolean showStale) {
      this.updateTargetFilterStateByUser(TeamCompassViewModel::setShowStale$lambda$41);
   }

   public final void setFocusMode(boolean enabled) {
      this.updateTargetFilterStateByUser(TeamCompassViewModel::setFocusMode$lambda$42);
   }

   private final void updateTargetFilterStateByUser(Function1 transform) {
      if (this.tacticalFiltersEnabled) {
         this.targetFilterDirtyByUser = true;
         MutableStateFlow $this$update$iv = this._ui;
         int $i$f$update = 0;

         Object prevValue$iv;
         Object nextValue$iv;
         do {
            prevValue$iv = $this$update$iv.getValue();
            UiState it = (UiState)prevValue$iv;
            int var6 = 0;
            nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, FilterUiState.copy$default(it.getFilter(), (TargetFilterState)transform.invoke(it.getFilter().getTargetFilterState()), (List)null, (List)null, 6, (Object)null), (SettingsUiState)null, (BluetoothUiState)null, (String)null, 239, (Object)null);
         } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

         refreshTargetsFromState$default(this, 0L, 1, (Object)null);
      }
   }

   public final void importTacticalMap(@NotNull Uri uri) {
      Intrinsics.checkNotNullParameter(uri, "uri");
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var6 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, TeamUiState.copy$default(it.getTeam(), (String)null, (String)null, (List)null, (List)null, (PlayerMode)null, 0L, (QuickCommand)null, (TeamViewMode)null, true, 255, (Object)null), (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 251, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new importTacticalMap.2(this, uri, (Continuation)null)), 2, (Object)null);
   }

   public final void clearTacticalMap() {
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var5 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, MapUiState.copy$default(it.getMap(), (List)null, (List)null, (List)null, (List)null, false, (QuickCommandType)null, (TacticalMap)null, false, 0.0F, 319, (Object)null), (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 247, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

   }

   public final void setMapEnabled(boolean enabled) {
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var6 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, MapUiState.copy$default(it.getMap(), (List)null, (List)null, (List)null, (List)null, false, (QuickCommandType)null, (TacticalMap)null, enabled, 0.0F, 383, (Object)null), (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 247, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

   }

   public final void setMapOpacity(float opacity) {
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var6 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, MapUiState.copy$default(it.getMap(), (List)null, (List)null, (List)null, (List)null, false, (QuickCommandType)null, (TacticalMap)null, false, RangesKt.coerceIn(opacity, 0.0F, 1.0F), 255, (Object)null), (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 247, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

   }

   public final void saveMapChangesToSource(@NotNull List newPoints, @NotNull List deletedPoints) {
      Intrinsics.checkNotNullParameter(newPoints, "newPoints");
      Intrinsics.checkNotNullParameter(deletedPoints, "deletedPoints");
      if (this.mapCoordinator.hasPointChanges(newPoints, deletedPoints)) {
         TacticalMap map = ((UiState)this._ui.getValue()).getActiveMap();
         if (map == null) {
            emitError$default(this, this.tr(string.map_not_loaded), (Throwable)null, 2, (Object)null);
         } else {
            Uri sourceUri = this.mapCoordinator.sourceUriOrNull(map);
            if (sourceUri == null) {
               emitError$default(this, this.tr(string.vm_error_map_source_no_access), (Throwable)null, 2, (Object)null);
            } else {
               this.saveMapChangesInternal(sourceUri, map, newPoints, deletedPoints);
            }
         }
      }
   }

   // $FF: synthetic method
   public static void saveMapChangesToSource$default(TeamCompassViewModel var0, List var1, List var2, int var3, Object var4) {
      if ((var3 & 2) != 0) {
         var2 = CollectionsKt.emptyList();
      }

      var0.saveMapChangesToSource(var1, var2);
   }

   public final void saveMapChangesAs(@NotNull Uri uri, @NotNull List newPoints, @NotNull List deletedPoints) {
      Intrinsics.checkNotNullParameter(uri, "uri");
      Intrinsics.checkNotNullParameter(newPoints, "newPoints");
      Intrinsics.checkNotNullParameter(deletedPoints, "deletedPoints");
      if (this.mapCoordinator.hasPointChanges(newPoints, deletedPoints)) {
         TacticalMap map = ((UiState)this._ui.getValue()).getActiveMap();
         if (map == null) {
            emitError$default(this, this.tr(string.map_not_loaded), (Throwable)null, 2, (Object)null);
         } else {
            this.saveMapChangesInternal(uri, map, newPoints, deletedPoints);
         }
      }
   }

   // $FF: synthetic method
   public static void saveMapChangesAs$default(TeamCompassViewModel var0, Uri var1, List var2, List var3, int var4, Object var5) {
      if ((var4 & 4) != 0) {
         var3 = CollectionsKt.emptyList();
      }

      var0.saveMapChangesAs(var1, var2, var3);
   }

   private final void saveMapChangesInternal(Uri destinationUri, TacticalMap map, List newPoints, List deletedPoints) {
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var9 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, TeamUiState.copy$default(it.getTeam(), (String)null, (String)null, (List)null, (List)null, (PlayerMode)null, 0L, (QuickCommand)null, (TeamViewMode)null, true, 255, (Object)null), (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 251, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new saveMapChangesInternal.2(this, map, newPoints, deletedPoints, destinationUri, (Continuation)null)), 2, (Object)null);
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
      long until = System.currentTimeMillis() + 60000L;
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var7 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, TeamUiState.copy$default(it.getTeam(), (String)null, (String)null, (List)null, (List)null, (PlayerMode)null, until, (QuickCommand)null, (TeamViewMode)null, false, 479, (Object)null), (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 251, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      this.trackingController.updateStatus(((UiState)this._ui.getValue()).getPlayerMode(), until, true);
   }

   public final void clearSos() {
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var5 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, TeamUiState.copy$default(it.getTeam(), (String)null, (String)null, (List)null, (List)null, (PlayerMode)null, 0L, (QuickCommand)null, (TeamViewMode)null, false, 479, (Object)null), (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 251, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      this.trackingController.updateStatus(((UiState)this._ui.getValue()).getPlayerMode(), 0L, true);
   }

   public final void addPointHere(@NotNull String label, @NotNull String icon, boolean forTeam) {
      Intrinsics.checkNotNullParameter(label, "label");
      Intrinsics.checkNotNullParameter(icon, "icon");
      LocationPoint var10000 = ((UiState)this._ui.getValue()).getMe();
      if (var10000 != null) {
         LocationPoint me = var10000;
         this.addPointAt(me.getLat(), me.getLon(), label, icon, forTeam);
      }
   }

   public final void addPointAt(double lat, double lon, @NotNull String label, @NotNull String icon, boolean forTeam) {
      Intrinsics.checkNotNullParameter(label, "label");
      Intrinsics.checkNotNullParameter(icon, "icon");
      this.getTacticalActionsCoordinator().addPointAt(lat, lon, label, icon, forTeam, this.tr(string.vm_error_add_point_failed), this.tr(string.error_invalid_input));
   }

   public final void updatePoint(@NotNull String id, double lat, double lon, @NotNull String label, @NotNull String icon, boolean isTeam) {
      Intrinsics.checkNotNullParameter(id, "id");
      Intrinsics.checkNotNullParameter(label, "label");
      Intrinsics.checkNotNullParameter(icon, "icon");
      this.getTacticalActionsCoordinator().updatePoint(id, lat, lon, label, icon, isTeam, this.tr(string.vm_error_only_author_edit_team_point), this.tr(string.vm_error_update_point_failed), this.tr(string.error_invalid_input));
   }

   public final void deletePoint(@NotNull String id, boolean isTeam) {
      Intrinsics.checkNotNullParameter(id, "id");
      this.getTacticalActionsCoordinator().deletePoint(id, isTeam, this.tr(string.vm_error_only_author_delete_team_point), this.tr(string.vm_error_delete_point_failed));
   }

   public final void sendQuickCommand(@NotNull QuickCommandType type) {
      Intrinsics.checkNotNullParameter(type, "type");
      this.getTacticalActionsCoordinator().sendQuickCommand(type);
   }

   public final void addEnemyPing(double lat, double lon, @NotNull QuickCommandType type) {
      Intrinsics.checkNotNullParameter(type, "type");
      this.getTacticalActionsCoordinator().addEnemyPing(lat, lon, type, this.tr(string.vm_error_enemy_mark_failed), this.tr(string.error_invalid_input));
   }

   public final void assignTeamMemberRole(@NotNull String targetUid, @NotNull TeamRolePatch patch) {
      Intrinsics.checkNotNullParameter(targetUid, "targetUid");
      Intrinsics.checkNotNullParameter(patch, "patch");
      UiState state = (UiState)this._ui.getValue();
      String var10000 = state.getTeamCode();
      if (var10000 != null) {
         String code = var10000;
         var10000 = state.getUid();
         if (var10000 != null) {
            String actorUid = var10000;
            if (!StringsKt.isBlank((CharSequence)targetUid)) {
               BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new assignTeamMemberRole.1(this, code, actorUid, targetUid, patch, (Continuation)null)), 2, (Object)null);
            }
         }
      }
   }

   public final void assignTeamMemberRolesBulk(@NotNull List targetUids, @NotNull TeamRolePatch patch) {
      Intrinsics.checkNotNullParameter(targetUids, "targetUids");
      Intrinsics.checkNotNullParameter(patch, "patch");
      UiState state = (UiState)this._ui.getValue();
      String var10000 = state.getTeamCode();
      if (var10000 != null) {
         String code = var10000;
         var10000 = state.getUid();
         if (var10000 != null) {
            String actorUid = var10000;
            if (patch.getCommandRole() != null || patch.getCombatRole() != null || patch.getVehicleRole() != null || patch.getOrgPath() != null) {
               List targets = SequencesKt.toList(SequencesKt.distinct(SequencesKt.filter(SequencesKt.map(CollectionsKt.asSequence((Iterable)targetUids), TeamCompassViewModel::assignTeamMemberRolesBulk$lambda$51), TeamCompassViewModel::assignTeamMemberRolesBulk$lambda$52)));
               if (!targets.isEmpty()) {
                  BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new assignTeamMemberRolesBulk.1(targets, this, code, actorUid, patch, (Continuation)null)), 2, (Object)null);
               }
            }
         }
      }
   }

   public final void createTeam() {
      if (!((UiState)this._ui.getValue()).isBusy()) {
         String uid = ((UiState)this._ui.getValue()).getUid();
         CharSequence var2 = (CharSequence)uid;
         if (var2 == null || StringsKt.isBlank(var2)) {
            emitError$default(this, this.tr(string.vm_error_auth_not_ready), (Throwable)null, 2, (Object)null);
            this.ensureAuth();
         } else {
            CharSequence var3 = (CharSequence)((UiState)this._ui.getValue()).getCallsign();
            Object var10000;
            if (StringsKt.isBlank(var3)) {
               int var4 = 0;
               var10000 = this.tr(string.default_callsign_player);
            } else {
               var10000 = var3;
            }

            String callsign = (String)var10000;
            MutableStateFlow $this$update$iv = this._ui;
            int $i$f$update = 0;

            Object prevValue$iv;
            Object nextValue$iv;
            do {
               prevValue$iv = $this$update$iv.getValue();
               UiState it = (UiState)prevValue$iv;
               int var7 = 0;
               nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, TeamUiState.copy$default(it.getTeam(), (String)null, (String)null, (List)null, (List)null, (PlayerMode)null, 0L, (QuickCommand)null, (TeamViewMode)null, true, 255, (Object)null), (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 251, (Object)null);
            } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

            BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new createTeam.2(this, uid, callsign, (Continuation)null)), 2, (Object)null);
         }
      }
   }

   public final void joinTeam(@NotNull String codeRaw, boolean alsoCreateMember) {
      Intrinsics.checkNotNullParameter(codeRaw, "codeRaw");
      if (!((UiState)this._ui.getValue()).isBusy()) {
         String uid = ((UiState)this._ui.getValue()).getUid();
         CharSequence var4 = (CharSequence)uid;
         if (var4 == null || StringsKt.isBlank(var4)) {
            emitError$default(this, this.tr(string.vm_error_auth_not_ready), (Throwable)null, 2, (Object)null);
            this.ensureAuth();
         } else {
            String code = this.normalizeTeamCode(codeRaw);
            if (code == null) {
               emitError$default(this, this.tr(string.join_code_error), (Throwable)null, 2, (Object)null);
            } else if (!this.joinRateLimiter.canAttempt(code)) {
               emitError$default(this, this.tr(string.vm_error_join_rate_limited), (Throwable)null, 2, (Object)null);
            } else {
               String traceId = this.newTraceId("joinTeam");
               this.logActionStart("joinTeam", traceId, code, uid);
               CharSequence var7 = (CharSequence)((UiState)this._ui.getValue()).getCallsign();
               Object var10000;
               if (StringsKt.isBlank(var7)) {
                  int var8 = 0;
                  var10000 = this.tr(string.default_callsign_player);
               } else {
                  var10000 = var7;
               }

               String callsign = (String)var10000;
               MutableStateFlow $this$update$iv = this._ui;
               int $i$f$update = 0;

               Object prevValue$iv;
               Object nextValue$iv;
               do {
                  prevValue$iv = $this$update$iv.getValue();
                  UiState it = (UiState)prevValue$iv;
                  int var11 = 0;
                  nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, TeamUiState.copy$default(it.getTeam(), (String)null, (String)null, (List)null, (List)null, (PlayerMode)null, 0L, (QuickCommand)null, (TeamViewMode)null, true, 255, (Object)null), (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 251, (Object)null);
               } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

               BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new joinTeam.2(this, code, uid, callsign, alsoCreateMember, traceId, (Continuation)null)), 2, (Object)null);
            }
         }
      }
   }

   // $FF: synthetic method
   public static void joinTeam$default(TeamCompassViewModel var0, String var1, boolean var2, int var3, Object var4) {
      if ((var3 & 2) != 0) {
         var2 = true;
      }

      var0.joinTeam(var1, var2);
   }

   private final Object onTeamJoined(String code, Continuation $completion) {
      label27: {
         if ($completion instanceof Continuation $continuation) {
            if (($continuation.label & Integer.MIN_VALUE) != 0) {
               $continuation.label -= Integer.MIN_VALUE;
               break label27;
            }
         }

         $continuation = new onTeamJoined.1(this, $completion);
      }

      Object $result = $continuation.result;
      Object var10 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
      switch ($continuation.label) {
         case 0:
            ResultKt.throwOnFailure($result);
            MutableStateFlow $this$update$iv = this._ui;
            int $i$f$update = 0;

            Object prevValue$iv;
            Object nextValue$iv;
            do {
               prevValue$iv = $this$update$iv.getValue();
               UiState it = (UiState)prevValue$iv;
               int var7 = 0;
               nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, TeamUiState.copy$default(it.getTeam(), (String)null, code, (List)null, (List)null, (PlayerMode)null, 0L, (QuickCommand)null, (TeamViewMode)null, false, 509, (Object)null), (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 123, (Object)null);
            } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

            UserPrefs var10000 = this.prefs;
            $continuation.L$0 = this;
            $continuation.L$1 = code;
            $continuation.label = 1;
            if (var10000.setTeamCode(code, $continuation) == var10) {
               return var10;
            }
            break;
         case 1:
            code = (String)$continuation.L$1;
            this = (TeamCompassViewModel)$continuation.L$0;
            ResultKt.throwOnFailure($result);
            break;
         default:
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
      }

      this.startListening(code);
      this.evaluateIdentityLinkingEligibility(code);
      return Unit.INSTANCE;
   }

   private final void evaluateIdentityLinkingEligibility(String teamCode) {
      String var10000 = ((UiState)this._ui.getValue()).getUid();
      if (var10000 != null) {
         String uid = var10000;
         if (!Intrinsics.areEqual(this.identityLinkingPromptTrackedUid, uid)) {
            String traceId = this.newTraceId("identityLinkingEligibility");
            this.logActionStart("identityLinkingEligibility", traceId, teamCode, uid);
            TeamCompassViewModel var4 = this;

            Object $this$evaluateIdentityLinkingEligibility_u24lambda_u2458;
            try {
               Result.Companion var15 = Result.Companion;
               TeamCompassViewModel $this$evaluateIdentityLinkingEligibility_u24lambda_u2458 = var4;
               int var6 = 0;
               $this$evaluateIdentityLinkingEligibility_u24lambda_u2458 = Result.constructor-impl($this$evaluateIdentityLinkingEligibility_u24lambda_u2458.identityLinkingService.evaluateEligibility());
            } catch (Throwable var8) {
               Result.Companion var14 = Result.Companion;
               $this$evaluateIdentityLinkingEligibility_u24lambda_u2458 = Result.constructor-impl(ResultKt.createFailure(var8));
            }

            Object var9 = $this$evaluateIdentityLinkingEligibility_u24lambda_u2458;
            if (Result.isSuccess-impl($this$evaluateIdentityLinkingEligibility_u24lambda_u2458)) {
               IdentityLinkingEligibility eligibility = (IdentityLinkingEligibility)$this$evaluateIdentityLinkingEligibility_u24lambda_u2458;
               int var13 = 0;
               if (eligibility.getShouldPrompt()) {
                  this.identityLinkingPromptTrackedUid = uid;
                  Log.i("TeamCompassVM", "Identity linking is eligible for uid=" + uid + " team=" + teamCode + " reason=" + eligibility.getReason());
               }

               this.logActionSuccess("identityLinkingEligibility", traceId, teamCode, uid);
            }

            Throwable var16 = Result.exceptionOrNull-impl(var9);
            if (var16 != null) {
               Throwable err = var16;
               int var7 = 0;
               this.logActionFailure("identityLinkingEligibility", traceId, err, err.getMessage(), teamCode, uid);
            }

         }
      }
   }

   public final void markCompassHelpSeen() {
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var5 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, (FilterUiState)null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0F, (ThemeMode)null, false, (Map)null, false, false, 1535, (Object)null), (BluetoothUiState)null, (String)null, 223, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new markCompassHelpSeen.2(this, (Continuation)null)), 2, (Object)null);
   }

   public final void markOnboardingSeen() {
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var5 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, (FilterUiState)null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0F, (ThemeMode)null, false, (Map)null, false, false, 1023, (Object)null), (BluetoothUiState)null, (String)null, 223, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new markOnboardingSeen.2(this, (Continuation)null)), 2, (Object)null);
   }

   public final void setControlLayoutEdit(boolean enabled) {
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var6 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, (FilterUiState)null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0F, (ThemeMode)null, enabled, (Map)null, false, false, 1919, (Object)null), (BluetoothUiState)null, (String)null, 223, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new setControlLayoutEdit.2(this, enabled, (Continuation)null)), 2, (Object)null);
   }

   public final void setControlPosition(@NotNull CompassControlId id, @NotNull ControlPosition position) {
      Intrinsics.checkNotNullParameter(id, "id");
      Intrinsics.checkNotNullParameter(position, "position");
      ControlPosition normalized = CompassControlLayoutKt.normalized(position);
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var8 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, (FilterUiState)null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0F, (ThemeMode)null, false, MapsKt.plus(it.getSettings().getControlPositions(), TuplesKt.to(id, normalized)), false, false, 1791, (Object)null), (BluetoothUiState)null, (String)null, 223, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new setControlPosition.2(this, id, normalized, (Continuation)null)), 2, (Object)null);
   }

   public final void resetControlPositions() {
      Map defaults = CompassControlLayoutKt.defaultCompassControlPositions();
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var6 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, (FilterUiState)null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0F, (ThemeMode)null, false, defaults, false, false, 1791, (Object)null), (BluetoothUiState)null, (String)null, 223, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new resetControlPositions.2(this, (Continuation)null)), 2, (Object)null);
   }

   public final void applyControlLayoutPreset(@NotNull ControlLayoutPreset preset) {
      Intrinsics.checkNotNullParameter(preset, "preset");
      Map positions = CompassControlLayoutKt.controlPositionsForPreset(preset);
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var7 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, (FilterUiState)null, SettingsUiState.copy$default(it.getSettings(), 0, 0, 0, 0, false, 0.0F, (ThemeMode)null, false, positions, false, false, 1791, (Object)null), (BluetoothUiState)null, (String)null, 223, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new applyControlLayoutPreset.2(positions, this, (Continuation)null)), 2, (Object)null);
   }

   public final void leaveTeam() {
      this.stopTracking();
      this.stopListening();
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var5 = 0;
         TeamUiState var6 = TeamUiState.copy$default(it.getTeam(), (String)null, (String)null, CollectionsKt.emptyList(), CollectionsKt.emptyList(), (PlayerMode)null, 0L, (QuickCommand)null, (TeamViewMode)null, false, 433, (Object)null);
         FilterUiState var7 = FilterUiState.copy$default(it.getFilter(), (TargetFilterState)null, CollectionsKt.emptyList(), CollectionsKt.emptyList(), 1, (Object)null);
         MapUiState var8 = MapUiState.copy$default(it.getMap(), CollectionsKt.emptyList(), CollectionsKt.emptyList(), CollectionsKt.emptyList(), CollectionsKt.emptyList(), false, (QuickCommandType)null, (TacticalMap)null, false, 0.0F, 496, (Object)null);
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, var6, var8, var7, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 227, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new leaveTeam.2(this, (Continuation)null)), 2, (Object)null);
   }

   private final void startListening(String codeRaw) {
      if (((UiState)this._ui.getValue()).isAuthReady()) {
         String var10000 = ((UiState)this._ui.getValue()).getUid();
         if (var10000 != null) {
            String uid = var10000;
            String traceId = this.newTraceId("startListening");
            this.logActionStart("startListening", traceId, codeRaw, uid);
            String code = this.normalizeTeamCode(codeRaw);
            if (code == null) {
               emitError$default(this, this.tr(string.vm_error_team_code_invalid), (Throwable)null, 2, (Object)null);
               logActionFailure$default(this, "startListening", traceId, (Throwable)null, "invalid team code", codeRaw, uid, 4, (Object)null);
               this.stopListening();
            } else {
               Job var5 = this.teamObserverJob;
               if (var5 != null) {
                  DefaultImpls.cancel$default(var5, (CancellationException)null, 1, (Object)null);
               }

               this.teamObserverJob = BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new startListening.1(this, code, uid, traceId, (Continuation)null)), 2, (Object)null);
               this.startMemberPrefsSync(code, uid);
            }
         }
      }
   }

   private final boolean handleStartListeningTerminalFailure(TeamActionFailure failure, String teamCode, String uid, String traceId) {
      Pair var10000;
      switch (com.example.teamcompass.ui.TeamCompassViewModel.WhenMappings.$EnumSwitchMapping$0[failure.getError().ordinal()]) {
         case 1:
            int var14 = string.vm_error_team_locked_format;
            Object[] var11 = new Object[]{teamCode};
            var10000 = TuplesKt.to(this.tr(var14, var11), "team locked");
            break;
         case 2:
            int var13 = string.vm_error_team_code_expired_format;
            Object[] var10 = new Object[]{teamCode};
            var10000 = TuplesKt.to(this.tr(var13, var10), "team expired");
            break;
         case 3:
            int var12 = string.vm_error_team_not_found_format;
            Object[] var9 = new Object[]{teamCode};
            var10000 = TuplesKt.to(this.tr(var12, var9), "team not found");
            break;
         case 4:
            int var10001 = string.vm_error_team_permission_denied_format;
            Object[] var8 = new Object[]{teamCode};
            var10000 = TuplesKt.to(this.tr(var10001, var8), "permission denied");
            break;
         default:
            return false;
      }

      Pair var5 = var10000;
      String userMessage = (String)var5.component1();
      String logReason = (String)var5.component2();
      emitError$default(this, userMessage, (Throwable)null, 2, (Object)null);
      logActionFailure$default(this, "startListening", traceId, (Throwable)null, logReason, teamCode, uid, 4, (Object)null);
      this.clearTeamSessionStateForTerminalFailure();
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new handleStartListeningTerminalFailure.1(this, (Continuation)null)), 2, (Object)null);
      return true;
   }

   private final void clearTeamSessionStateForTerminalFailure() {
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var5 = 0;
         TeamUiState var6 = TeamUiState.copy$default(it.getTeam(), (String)null, (String)null, CollectionsKt.emptyList(), CollectionsKt.emptyList(), (PlayerMode)null, 0L, (QuickCommand)null, (TeamViewMode)null, false, 433, (Object)null);
         FilterUiState var7 = FilterUiState.copy$default(it.getFilter(), (TargetFilterState)null, CollectionsKt.emptyList(), CollectionsKt.emptyList(), 1, (Object)null);
         MapUiState var8 = MapUiState.copy$default(it.getMap(), CollectionsKt.emptyList(), CollectionsKt.emptyList(), CollectionsKt.emptyList(), CollectionsKt.emptyList(), false, (QuickCommandType)null, (TacticalMap)null, false, 0.0F, 480, (Object)null);
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, var6, var8, var7, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 227, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

   }

   private final Object collectTeamSnapshotsWithReconnect(String teamCode, String uid, Continuation $completion) {
      String backendDownMessage = this.tr(string.vm_error_backend_unavailable_retrying);
      Object var10000 = this.teamSnapshotObserver.collectWithReconnect(teamCode, uid, TeamCompassViewModel::collectTeamSnapshotsWithReconnect$lambda$69, TeamCompassViewModel::collectTeamSnapshotsWithReconnect$lambda$70, (Function2)(new collectTeamSnapshotsWithReconnect.4(uid, this, teamCode, (Continuation)null)), (Function3)(new collectTeamSnapshotsWithReconnect.5(this, backendDownMessage, (Continuation)null)), $completion);
      return var10000 == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? var10000 : Unit.INSTANCE;
   }

   private final void stopListening() {
      Job var10000 = this.teamObserverJob;
      if (var10000 != null) {
         DefaultImpls.cancel$default(var10000, (CancellationException)null, 1, (Object)null);
      }

      this.teamObserverJob = null;
      var10000 = this.p2pObserverJob;
      if (var10000 != null) {
         DefaultImpls.cancel$default(var10000, (CancellationException)null, 1, (Object)null);
      }

      this.p2pObserverJob = null;
      this.backendHealthDelegate.stop();
      this.lastBackendHealthAvailableSample = true;
      var10000 = this.memberPrefsObserverJob;
      if (var10000 != null) {
         DefaultImpls.cancel$default(var10000, (CancellationException)null, 1, (Object)null);
      }

      this.memberPrefsObserverJob = null;
      var10000 = this.memberPrefsSyncJob;
      if (var10000 != null) {
         DefaultImpls.cancel$default(var10000, (CancellationException)null, 1, (Object)null);
      }

      this.memberPrefsSyncJob = null;
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var5 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, TrackingUiState.copy$default(it.getTracking(), false, false, false, (LocationPoint)null, (Double)null, (TrackingMode)null, false, false, TelemetryState.copy$default(it.getTracking().getTelemetry(), 0, 0, 0, 0L, (String)null, true, 0L, 0L, false, 0, 0, 1567, (Object)null), 255, (Object)null), (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 253, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

   }

   private final void startP2PInboundObservation(String teamCode, String localUid) {
      Job var10000 = this.p2pObserverJob;
      if (var10000 != null) {
         DefaultImpls.cancel$default(var10000, (CancellationException)null, 1, (Object)null);
      }

      this.p2pObserverJob = BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new startP2PInboundObservation.1(this, teamCode, localUid, (Continuation)null)), 2, (Object)null);
   }

   private final void handleP2PInbound(P2PInboundMessage inbound, String localUid) {
      String senderId = inbound.getMessage().getMetadata().getSenderId();
      if (!Intrinsics.areEqual(senderId, localUid)) {
         MutableStateFlow $this$update$iv = this._ui;
         int $i$f$update = 0;

         Object prevValue$iv;
         Object nextValue$iv;
         do {
            prevValue$iv = $this$update$iv.getValue();
            UiState state = (UiState)prevValue$iv;
            int var8 = 0;
            nextValue$iv = UiState.copy$default(state, (AuthState)null, TrackingUiState.copy$default(state.getTracking(), false, false, false, (LocationPoint)null, (Double)null, (TrackingMode)null, false, false, TelemetryState.copy$default(state.getTelemetry(), 0, 0, 0, 0L, (String)null, false, 0L, 0L, false, state.getTelemetry().getP2pInboundMessages() + 1, 0, 1535, (Object)null), 255, (Object)null), (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 253, (Object)null);
         } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

         String var10001 = inbound.getTransportName();
         Log.i("TeamCompassVM", "P2P inbound via " + var10001 + ": type=" + inbound.getMessage().getMetadata().getType() + " sender=" + senderId);
      }
   }

   private final void startBackendHealthMonitor() {
      String backendDownMessage = this.tr(string.vm_error_backend_unavailable_retrying);
      this.backendHealthDelegate.startHealthMonitor(ViewModelKt.getViewModelScope((ViewModel)this), (Function4)(new startBackendHealthMonitor.1(this, backendDownMessage, (Continuation)null)));
   }

   private final void startBackendStaleMonitor() {
      scheduleBackendStaleRefresh$default(this, 0L, 1, (Object)null);
   }

   private final void scheduleBackendStaleRefresh(long nowMs) {
      this.backendHealthDelegate.scheduleStaleRefresh(ViewModelKt.getViewModelScope((ViewModel)this), nowMs, TeamCompassViewModel::scheduleBackendStaleRefresh$lambda$73, TeamCompassViewModel::scheduleBackendStaleRefresh$lambda$74);
   }

   // $FF: synthetic method
   static void scheduleBackendStaleRefresh$default(TeamCompassViewModel var0, long var1, int var3, Object var4) {
      if ((var3 & 1) != 0) {
         var1 = System.currentTimeMillis();
      }

      var0.scheduleBackendStaleRefresh(var1);
   }

   private final void refreshBackendStaleFlag(long nowMs) {
      String backendDownMessage = this.tr(string.vm_error_backend_unavailable_retrying);
      boolean becameUnavailable = false;
      boolean becameRecovered = false;
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState state = (UiState)prevValue$iv;
         int var10 = 0;
         TelemetryState telemetry = state.getTracking().getTelemetry();
         boolean stale = this.computeBackendStale(telemetry.getLastSnapshotAtMs(), nowMs);
         boolean hasFreshSnapshot = telemetry.getLastSnapshotAtMs() > 0L && !stale;
         boolean effectiveAvailable = this.lastBackendHealthAvailableSample || hasFreshSnapshot;
         long var10000;
         if (effectiveAvailable) {
            var10000 = 0L;
         } else {
            Long var15 = telemetry.getBackendUnavailableSinceMs();
            long it = ((Number)var15).longValue();
            int var18 = 0;
            var10000 = (it > 0L ? var15 : null) != null ? it > 0L ? var15 : null : nowMs;
         }

         long unavailableSinceMs = var10000;
         boolean changed = stale != telemetry.isBackendStale() || effectiveAvailable != telemetry.getBackendAvailable() || unavailableSinceMs != telemetry.getBackendUnavailableSinceMs();
         UiState var29;
         if (!changed) {
            var29 = state;
         } else {
            becameUnavailable = telemetry.getBackendAvailable() && !effectiveAvailable;
            becameRecovered = !telemetry.getBackendAvailable() && effectiveAvailable;
            var29 = UiState.copy$default(state, (AuthState)null, TrackingUiState.copy$default(state.getTracking(), false, false, false, (LocationPoint)null, (Double)null, (TrackingMode)null, false, false, TelemetryState.copy$default(telemetry, 0, 0, 0, 0L, (String)null, effectiveAvailable, unavailableSinceMs, 0L, stale, 0, 0, 1695, (Object)null), 255, (Object)null), (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 253, (Object)null);
         }

         nextValue$iv = var29;
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      if (becameUnavailable) {
         emitError$default(this, backendDownMessage, (Throwable)null, 2, (Object)null);
      } else if (becameRecovered) {
         $this$update$iv = this._ui;
         $i$f$update = 0;

         Object nextValue$iv;
         do {
            prevValue$iv = $this$update$iv.getValue();
            UiState state = (UiState)prevValue$iv;
            int var28 = 0;
            nextValue$iv = Intrinsics.areEqual(state.getLastError(), backendDownMessage) ? UiState.copy$default(state, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 127, (Object)null) : state;
         } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));
      }

   }

   // $FF: synthetic method
   static void refreshBackendStaleFlag$default(TeamCompassViewModel var0, long var1, int var3, Object var4) {
      if ((var3 & 1) != 0) {
         var1 = System.currentTimeMillis();
      }

      var0.refreshBackendStaleFlag(var1);
   }

   private final boolean computeBackendStale(long lastSnapshotAtMs, long nowMs) {
      return this.backendHealthDelegate.computeBackendStale(lastSnapshotAtMs, nowMs);
   }

   private final void startMemberPrefsSync(String teamCode, String uid) {
      Job var10000 = this.memberPrefsObserverJob;
      if (var10000 != null) {
         DefaultImpls.cancel$default(var10000, (CancellationException)null, 1, (Object)null);
      }

      this.memberPrefsObserverJob = null;
      var10000 = this.memberPrefsSyncJob;
      if (var10000 != null) {
         DefaultImpls.cancel$default(var10000, (CancellationException)null, 1, (Object)null);
      }

      this.memberPrefsSyncJob = null;
      MemberPrefsSyncWorker var10 = this.memberPrefsSyncWorker;
      CoroutineScope var10001 = ViewModelKt.getViewModelScope((ViewModel)this);
      Flow $this$map$iv = (Flow)this.ui;
      int $i$f$map = 0;
      int $i$f$unsafeTransform = 0;
      int $i$f$unsafeFlow = 0;
      MemberPrefsSyncJobs jobs = var10.start(var10001, teamCode, uid, (Flow)(new startMemberPrefsSync..inlined.map.1($this$map$iv)), TeamCompassViewModel::startMemberPrefsSync$lambda$79, TeamCompassViewModel::startMemberPrefsSync$lambda$81, TeamCompassViewModel::startMemberPrefsSync$lambda$82, TeamCompassViewModel::startMemberPrefsSync$lambda$83, TeamCompassViewModel::startMemberPrefsSync$lambda$84);
      this.memberPrefsObserverJob = jobs.getObserverJob();
      this.memberPrefsSyncJob = jobs.getSyncJob();
   }

   private final void processEnemyPingAlerts(List enemyPings) {
      Iterable $this$filterNot$iv = (Iterable)enemyPings;
      int $i$f$filterNot = 0;
      Collection destination$iv$iv = (Collection)(new ArrayList());
      int $i$f$filterNotTo = 0;

      for(Object element$iv$iv : $this$filterNot$iv) {
         EnemyPing it = (EnemyPing)element$iv$iv;
         int var11 = 0;
         if (!it.isBluetooth()) {
            destination$iv$iv.add(element$iv$iv);
         }
      }

      List tacticalEnemyPings = (List)destination$iv$iv;
      long now = System.currentTimeMillis();
      int closeAlerts = this.alertsCoordinator.consumeNewCloseEnemyPings(tacticalEnemyPings, ((UiState)this._ui.getValue()).getMe(), now);

      for(int var13 = 0; var13 < closeAlerts; ++var13) {
         int var14 = 0;
         this.vibrateAndBeep(true);
      }

   }

   private final void processSosAlerts() {
      long now = System.currentTimeMillis();
      List players = ((UiState)this._ui.getValue()).getPlayers();
      Iterable $this$forEach$iv = (Iterable)players;
      int $i$f$forEach = 0;

      for(Object element$iv : $this$forEach$iv) {
         PlayerState player = (PlayerState)element$iv;
         int var9 = 0;
         if (player.getSosUntilMs() > now) {
            this.eventNotificationManager.showSosAlert(player);
         }
      }

   }

   private final void vibrateAndBeep(boolean strong) {
      boolean hasVibratePermission = ContextCompat.checkSelfPermission((Context)this.application, "android.permission.VIBRATE") == 0;
      if (hasVibratePermission) {
         try {
            if (strong) {
               long[] timings = new long[]{0L, 90L, 60L, 120L, 60L, 220L};
               Vibrator var10000 = this.vibrator;
               if (var10000 != null) {
                  var10000.vibrate(VibrationEffect.createWaveform(timings, -1));
               }
            } else {
               Vibrator var8 = this.vibrator;
               if (var8 != null) {
                  var8.vibrate(VibrationEffect.createOneShot(200L, -1));
               }
            }
         } catch (Throwable err) {
            Log.w("TeamCompassVM", "Failed to vibrate alert", err);
         }
      }

      try {
         int toneType = strong ? 21 : 24;
         int durationMs = strong ? 450 : 160;
         this.tone.startTone(toneType, durationMs);
      } catch (Throwable err) {
         Log.w("TeamCompassVM", "Failed to play alert tone", err);
      }

   }

   private final void startDeadReminder() {
      if (this.deadReminderJob == null) {
         this.deadReminderJob = BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new startDeadReminder.1(this, (Continuation)null)), 2, (Object)null);
      }
   }

   private final void stopDeadReminder() {
      Job var10000 = this.deadReminderJob;
      if (var10000 != null) {
         DefaultImpls.cancel$default(var10000, (CancellationException)null, 1, (Object)null);
      }

      this.deadReminderJob = null;
   }

   public final void startTracking(@NotNull TrackingMode mode, boolean persistMode) {
      Intrinsics.checkNotNullParameter(mode, "mode");
      if (!this.locationReadinessCoordinator.hasLocationPermission()) {
         emitError$default(this, this.tr(string.vm_error_location_permission_required), (Throwable)null, 2, (Object)null);
         MutableStateFlow $this$update$iv = this._ui;
         int $i$f$update = 0;

         Object prevValue$iv;
         Object nextValue$iv;
         do {
            prevValue$iv = $this$update$iv.getValue();
            UiState it = (UiState)prevValue$iv;
            int $i$f$update = 0;
            nextValue$iv = UiState.copy$default(it, (AuthState)null, TrackingUiState.copy$default(it.getTracking(), false, false, false, (LocationPoint)null, (Double)null, (TrackingMode)null, false, false, (TelemetryState)null, 509, (Object)null), (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 253, (Object)null);
         } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      } else {
         MutableStateFlow $this$update$iv = this._ui;
         int $i$f$update = 0;

         Object prevValue$iv;
         Object nextValue$iv;
         do {
            prevValue$iv = $this$update$iv.getValue();
            UiState state = (UiState)prevValue$iv;
            int var7 = 0;
            nextValue$iv = this.locationReadinessCoordinator.applyServiceState(state);
         } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

         if (!((UiState)this._ui.getValue()).isLocationServiceEnabled()) {
            emitError$default(this, this.tr(string.vm_error_location_services_disabled), (Throwable)null, 2, (Object)null);
         } else {
            FirebaseAnalytics var10000 = this.analytics;
            Bundle $this$update$iv = new Bundle();
            String var12 = "start_tracking";
            FirebaseAnalytics var11 = var10000;
            int prevValue$iv = 0;
            $this$update$iv.putString("mode", mode.name());
            $this$update$iv.putString("team_code", ((UiState)this._ui.getValue()).getTeamCode());
            Unit var13 = Unit.INSTANCE;
            var11.logEvent(var12, $this$update$iv);
            UiState state = (UiState)this._ui.getValue();
            String var29 = state.getTeamCode();
            if (var29 != null) {
               String code = var29;
               var29 = state.getUid();
               if (var29 != null) {
                  String uid = var29;
                  if (persistMode) {
                     MutableStateFlow $this$update$iv = this._ui;
                     int $i$f$update = 0;

                     Object prevValue$iv;
                     Object nextValue$iv;
                     do {
                        prevValue$iv = $this$update$iv.getValue();
                        UiState it = (UiState)prevValue$iv;
                        int var10 = 0;
                        nextValue$iv = UiState.copy$default(it, (AuthState)null, TrackingUiState.copy$default(it.getTracking(), false, false, false, (LocationPoint)null, (Double)null, mode, false, false, (TelemetryState)null, 479, (Object)null), (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 253, (Object)null);
                     } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

                     BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new startTracking.5(this, mode, (Continuation)null)), 2, (Object)null);
                  }

                  this.startHeading();
                  TrackingCoordinator.start$default(this.trackingCoordinator, new TrackingCoordinator.StartRequest(code, uid, state.getCallsign(), mode, state.getGameIntervalSec(), state.getGameDistanceM(), state.getSilentIntervalSec(), state.getSilentDistanceM(), state.getPlayerMode(), state.getMySosUntilMs()), state.isTracking(), false, 4, (Object)null);
                  if (state.getPlayerMode() == PlayerMode.DEAD) {
                     this.startDeadReminder();
                  } else {
                     this.stopDeadReminder();
                  }

               }
            }
         }
      }
   }

   // $FF: synthetic method
   public static void startTracking$default(TeamCompassViewModel var0, TrackingMode var1, boolean var2, int var3, Object var4) {
      if ((var3 & 2) != 0) {
         var2 = true;
      }

      var0.startTracking(var1, var2);
   }

   private final void restartTracking() {
      if (((UiState)this._ui.getValue()).isTracking()) {
         this.startTracking(((UiState)this._ui.getValue()).getDefaultMode(), false);
      }
   }

   public final void stopTracking() {
      this.trackingCoordinator.stop();
      this.stopHeading();
      this.stopDeadReminder();
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var5 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, TrackingUiState.copy$default(it.getTracking(), false, false, false, (LocationPoint)null, (Double)null, (TrackingMode)null, false, false, (TelemetryState)null, 382, (Object)null), (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 253, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

   }

   public final void dismissError() {
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var5 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 127, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

   }

   @NotNull
   public final List computeTargets(long nowMs) {
      UiState state = (UiState)this._ui.getValue();
      Pair var4 = this.targetFilterCoordinator.buildTargetsForState(state, nowMs);
      List prioritized = (List)var4.component1();
      List display = (List)var4.component2();
      if (!Intrinsics.areEqual(state.getPrioritizedTargets(), prioritized) || !Intrinsics.areEqual(state.getDisplayTargets(), display)) {
         MutableStateFlow $this$update$iv = this._ui;
         int $i$f$update = 0;

         Object prevValue$iv;
         Object nextValue$iv;
         do {
            prevValue$iv = $this$update$iv.getValue();
            UiState it = (UiState)prevValue$iv;
            int var11 = 0;
            nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, FilterUiState.copy$default(it.getFilter(), (TargetFilterState)null, prioritized, display, 1, (Object)null), (SettingsUiState)null, (BluetoothUiState)null, (String)null, 239, (Object)null);
         } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));
      }

      return display;
   }

   private final void refreshTargetsFromState(long nowMs) {
      UiState state = (UiState)this._ui.getValue();
      Pair var4 = this.targetFilterCoordinator.buildTargetsForState(state, nowMs);
      List prioritized = (List)var4.component1();
      List display = (List)var4.component2();
      if (!Intrinsics.areEqual(state.getPrioritizedTargets(), prioritized) || !Intrinsics.areEqual(state.getDisplayTargets(), display)) {
         MutableStateFlow $this$update$iv = this._ui;
         int $i$f$update = 0;

         Object prevValue$iv;
         Object nextValue$iv;
         do {
            prevValue$iv = $this$update$iv.getValue();
            UiState it = (UiState)prevValue$iv;
            int var11 = 0;
            nextValue$iv = UiState.copy$default(it, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, FilterUiState.copy$default(it.getFilter(), (TargetFilterState)null, prioritized, display, 1, (Object)null), (SettingsUiState)null, (BluetoothUiState)null, (String)null, 239, (Object)null);
         } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      }
   }

   // $FF: synthetic method
   static void refreshTargetsFromState$default(TeamCompassViewModel var0, long var1, int var3, Object var4) {
      if ((var3 & 1) != 0) {
         var1 = System.currentTimeMillis();
      }

      var0.refreshTargetsFromState(var1);
   }

   private final void startHeading() {
      this.getHeadingSensorCoordinator().start();
   }

   private final void stopHeading() {
      this.getHeadingSensorCoordinator().stop();
   }

   private final void startLocationServiceMonitor() {
      Job var10000 = this.locationServiceMonitorJob;
      if (var10000 != null) {
         DefaultImpls.cancel$default(var10000, (CancellationException)null, 1, (Object)null);
      }

      this.locationServiceMonitorJob = this.locationReadinessCoordinator.startLocationServiceMonitor(ViewModelKt.getViewModelScope((ViewModel)this), TeamCompassViewModel::startLocationServiceMonitor$lambda$96, TeamCompassViewModel::startLocationServiceMonitor$lambda$97, TeamCompassViewModel::startLocationServiceMonitor$lambda$98, this.tr(string.vm_error_location_services_disabled), this.tr(string.vm_error_location_disabled_during_tracking), (Function1)(new startLocationServiceMonitor.4(Companion)));
   }

   private final String normalizeTeamCode(String raw) {
      return this.teamSessionDelegate.normalizeTeamCode(raw);
   }

   private final String tr(@StringRes int resId, Object... args) {
      String var10000 = this.application.getString(resId, Arrays.copyOf(args, args.length));
      Intrinsics.checkNotNullExpressionValue(var10000, "getString(...)");
      return var10000;
   }

   private final void handleActionFailure(String defaultMessage, TeamActionFailure failure) {
      String message = TeamActionErrorPolicy.INSTANCE.toUserMessage((Context)this.getApplication(), defaultMessage, failure);
      this.emitError(message, failure.getCause());
   }

   public final void logPerfMetricsSnapshot() {
      TeamCompassPerfSnapshot snapshot = TeamCompassPerfMetrics.INSTANCE.snapshot();
      long var10001 = snapshot.getRtdbSnapshotEmits();
      long var10002 = snapshot.getRtdbCleanupSweeps();
      long var10003 = snapshot.getRtdbCleanupWrites();
      long var10004 = snapshot.getMapBitmapLoadRequests();
      long var10005 = snapshot.getMapBitmapCacheHits();
      long var10006 = snapshot.getMapBitmapDecodes();
      String var2 = "%.1f";
      Object[] var3 = new Object[]{snapshot.getAverageMapBitmapDecodeMs()};
      String var10007 = String.format(var2, Arrays.copyOf(var3, var3.length));
      Intrinsics.checkNotNullExpressionValue(var10007, "format(...)");
      long var10008 = snapshot.getFullscreenMapFirstRenderSamples();
      var2 = "%.1f";
      var3 = new Object[]{snapshot.getAverageFullscreenMapFirstRenderMs()};
      String var10009 = String.format(var2, Arrays.copyOf(var3, var3.length));
      Intrinsics.checkNotNullExpressionValue(var10009, "format(...)");
      Log.i("TeamCompassVM", "perf snapshot: rtdbEmits=" + var10001 + ", cleanupSweeps=" + var10002 + ", cleanupWrites=" + var10003 + ", mapLoads=" + var10004 + ", mapHits=" + var10005 + ", mapDecodes=" + var10006 + ", mapAvgDecodeMs=" + var10007 + ", firstRenderSamples=" + var10008 + ", firstRenderAvgMs=" + var10009 + ", peakUsedMemBytes=" + snapshot.getPeakAppUsedMemoryBytes());
   }

   protected void onCleared() {
      this.stopHeading();
      this.stopListening();
      this.stopDeadReminder();
      Job var10000 = this.locationServiceMonitorJob;
      if (var10000 != null) {
         DefaultImpls.cancel$default(var10000, (CancellationException)null, 1, (Object)null);
      }

      this.locationServiceMonitorJob = null;
      this.autoBrightnessBinding.clear();
      if (this.bluetoothScanCoordinatorLazy.isInitialized()) {
         this.getBluetoothScanCoordinator().shutdown();
      }

      try {
         this.tone.release();
      } catch (Throwable err) {
         Log.w("TeamCompassVM", "Failed to release ToneGenerator", err);
      }

      super.onCleared();
   }

   public final void bindAutoBrightnessWindow(@Nullable Window window) {
      this.autoBrightnessBinding.bindWindow(window, ((UiState)this._ui.getValue()).getScreenBrightness(), ((UiState)this._ui.getValue()).getAutoBrightnessEnabled());
   }

   public final void setAutoBrightnessEnabled(boolean enabled) {
      this.autoBrightnessBinding.setEnabled(enabled);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new setAutoBrightnessEnabled.1(this, enabled, (Continuation)null)), 2, (Object)null);
   }

   public final void setScreenBrightness(float brightness) {
      this.autoBrightnessBinding.setBrightness(brightness);
      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new setScreenBrightness.1(this, brightness, (Continuation)null)), 2, (Object)null);
   }

   public final void setHasStartedOnce(boolean value) {
      MutableStateFlow $this$update$iv = this._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var6 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, TrackingUiState.copy$default(it.getTracking(), false, false, false, (LocationPoint)null, (Double)null, (TrackingMode)null, value, false, (TelemetryState)null, 447, (Object)null), (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 253, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      BuildersKt.launch$default(ViewModelKt.getViewModelScope((ViewModel)this), (CoroutineContext)this.coroutineExceptionHandler, (CoroutineStart)null, (Function2)(new setHasStartedOnce.2(this, value, (Continuation)null)), 2, (Object)null);
   }

   public final void autoStartTrackingIfNeeded() {
      if (!((UiState)this._ui.getValue()).getHasStartedOnce() && !((UiState)this._ui.getValue()).isTracking()) {
         this.setHasStartedOnce(true);
         startTracking$default(this, ((UiState)this._ui.getValue()).getDefaultMode(), false, 2, (Object)null);
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
      Intrinsics.checkNotNullParameter(err, "err");
      Log.w("TeamCompassVM", "Failed to initialize ScreenAutoBrightness", err);
      return Unit.INSTANCE;
   }

   private static final Unit headingSensorCoordinator_delegate$lambda$6$lambda$5(TeamCompassViewModel this$0, Double heading) {
      this$0.trackingController.updateHeading(heading);
      MutableStateFlow $this$update$iv = this$0._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var6 = 0;
         nextValue$iv = UiState.copy$default(it, (AuthState)null, TrackingUiState.copy$default(it.getTracking(), false, false, false, (LocationPoint)null, heading, (TrackingMode)null, false, false, (TelemetryState)null, 495, (Object)null), (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 253, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      return Unit.INSTANCE;
   }

   private static final HeadingSensorCoordinator headingSensorCoordinator_delegate$lambda$6(TeamCompassViewModel this$0) {
      SensorManager sensorManager = (SensorManager)this$0.application.getSystemService(SensorManager.class);
      return new HeadingSensorCoordinator(sensorManager, (DisplayManager)this$0.application.getSystemService(DisplayManager.class), sensorManager != null ? sensorManager.getDefaultSensor(11) : null, TeamCompassViewModel::headingSensorCoordinator_delegate$lambda$6$lambda$5);
   }

   private static final UiState bluetoothScanCoordinatorLazy$lambda$10$lambda$7(TeamCompassViewModel this$0) {
      return (UiState)this$0._ui.getValue();
   }

   private static final Unit bluetoothScanCoordinatorLazy$lambda$10$lambda$8(TeamCompassViewModel this$0, Function1 transform) {
      Intrinsics.checkNotNullParameter(transform, "transform");
      MutableStateFlow $this$update$iv = this$0._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         nextValue$iv = transform.invoke(prevValue$iv);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      return Unit.INSTANCE;
   }

   private static final Unit bluetoothScanCoordinatorLazy$lambda$10$lambda$9(TeamCompassViewModel this$0, String message) {
      Intrinsics.checkNotNullParameter(message, "message");
      emitError$default(this$0, message, (Throwable)null, 2, (Object)null);
      return Unit.INSTANCE;
   }

   private static final BluetoothScanCoordinator bluetoothScanCoordinatorLazy$lambda$10(TeamCompassViewModel this$0) {
      return new BluetoothScanCoordinator(this$0.application, this$0.teamRepository, ViewModelKt.getViewModelScope((ViewModel)this$0), TeamCompassViewModel::bluetoothScanCoordinatorLazy$lambda$10$lambda$7, TeamCompassViewModel::bluetoothScanCoordinatorLazy$lambda$10$lambda$8, TeamCompassViewModel::bluetoothScanCoordinatorLazy$lambda$10$lambda$9, (BluetoothScanner)null, 64, (DefaultConstructorMarker)null);
   }

   private static final UiState tacticalActionsCoordinator_delegate$lambda$14$lambda$11(TeamCompassViewModel this$0) {
      return (UiState)this$0._ui.getValue();
   }

   private static final Unit tacticalActionsCoordinator_delegate$lambda$14$lambda$12(TeamCompassViewModel this$0, Function1 transform) {
      Intrinsics.checkNotNullParameter(transform, "transform");
      MutableStateFlow $this$update$iv = this$0._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         nextValue$iv = transform.invoke(prevValue$iv);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      return Unit.INSTANCE;
   }

   private static final Unit tacticalActionsCoordinator_delegate$lambda$14$lambda$13(TeamCompassViewModel this$0, String message) {
      Intrinsics.checkNotNullParameter(message, "message");
      emitError$default(this$0, message, (Throwable)null, 2, (Object)null);
      return Unit.INSTANCE;
   }

   private static final TacticalActionsCoordinator tacticalActionsCoordinator_delegate$lambda$14(TeamCompassViewModel this$0) {
      return new TacticalActionsCoordinator(this$0.teamRepository, ViewModelKt.getViewModelScope((ViewModel)this$0), TeamCompassViewModel::tacticalActionsCoordinator_delegate$lambda$14$lambda$11, TeamCompassViewModel::tacticalActionsCoordinator_delegate$lambda$14$lambda$12, TeamCompassViewModel::tacticalActionsCoordinator_delegate$lambda$14$lambda$13, (Function2)(new tacticalActionsCoordinator.2.4(this$0)), (Function1)(new tacticalActionsCoordinator.2.5(this$0)), (Function4)(new tacticalActionsCoordinator.2.6(this$0)), (Function4)(new tacticalActionsCoordinator.2.7(this$0)), (Function6)(new tacticalActionsCoordinator.2.8(this$0)), 0L, 1024, (DefaultConstructorMarker)null);
   }

   private static final Unit ensureAuth$lambda$17(TeamCompassViewModel this$0, String $traceId, String uid) {
      this$0.onAuthReady(uid);
      logActionSuccess$default(this$0, "ensureAuth", $traceId, (String)null, uid, 4, (Object)null);
      return Unit.INSTANCE;
   }

   private static final Unit ensureAuth$lambda$19(TeamCompassViewModel this$0, String $traceId, Throwable err) {
      Intrinsics.checkNotNullParameter(err, "err");
      MutableStateFlow $this$update$iv = this$0._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState it = (UiState)prevValue$iv;
         int var7 = 0;
         nextValue$iv = UiState.copy$default(it, AuthState.copy$default(it.getAuth(), true, (String)null, 2, (Object)null), (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 126, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      int var10002 = string.vm_error_auth_failed_format;
      Object[] var8 = new Object[1];
      String var10005 = err.getMessage();
      if (var10005 == null) {
         var10005 = "";
      }

      var8[0] = var10005;
      this$0.emitError(this$0.tr(var10002, var8), err);
      logActionFailure$default(this$0, "ensureAuth", $traceId, err, err.getMessage(), (String)null, (String)null, 48, (Object)null);
      return Unit.INSTANCE;
   }

   private static final Unit refreshLocationPreview$lambda$33(TeamCompassViewModel this$0, Location loc) {
      if (loc == null) {
         return Unit.INSTANCE;
      } else {
         MutableStateFlow $this$update$iv = this$0._ui;
         int $i$f$update = 0;

         Object prevValue$iv;
         Object nextValue$iv;
         do {
            prevValue$iv = $this$update$iv.getValue();
            UiState it = (UiState)prevValue$iv;
            int var6 = 0;
            TrackingUiState var10002 = it.getTracking();
            LocationPoint var10006 = new LocationPoint;
            double var10008 = loc.getLatitude();
            double var10009 = loc.getLongitude();
            double var10010 = (double)loc.getAccuracy();
            double var10011 = (double)loc.getSpeed();
            Double var10012 = it.getMyHeadingDeg();
            if (var10012 == null) {
               var10012 = loc.hasBearing() ? (double)loc.getBearing() : null;
            }

            var10006.<init>(var10008, var10009, var10010, var10011, var10012, System.currentTimeMillis());
            nextValue$iv = UiState.copy$default(it, (AuthState)null, TrackingUiState.copy$default(var10002, false, false, false, var10006, (Double)null, (TrackingMode)null, false, false, (TelemetryState)null, 503, (Object)null), (TeamUiState)null, (MapUiState)null, (FilterUiState)null, (SettingsUiState)null, (BluetoothUiState)null, (String)null, 253, (Object)null);
         } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

         refreshTargetsFromState$default(this$0, 0L, 1, (Object)null);
         return Unit.INSTANCE;
      }
   }

   private static final void refreshLocationPreview$lambda$34(Function1 $tmp0, Object p0) {
      $tmp0.invoke(p0);
   }

   private static final void refreshLocationPreview$lambda$35(Exception e) {
      Intrinsics.checkNotNullParameter(e, "e");
      Log.w("TeamCompassVM", "lastLocation failed", (Throwable)e);
   }

   private static final TargetFilterState setTargetPreset$lambda$38(TargetFilterPreset $preset, TargetFilterState it) {
      Intrinsics.checkNotNullParameter(it, "it");
      return TargetFilterState.copy$default(it, $preset, 0, false, false, false, 30, (Object)null);
   }

   private static final TargetFilterState setNearRadius$lambda$39(int $safeRadius, TargetFilterState it) {
      Intrinsics.checkNotNullParameter(it, "it");
      return TargetFilterState.copy$default(it, (TargetFilterPreset)null, $safeRadius, false, false, false, 29, (Object)null);
   }

   private static final TargetFilterState setShowDead$lambda$40(boolean $showDead, TargetFilterState it) {
      Intrinsics.checkNotNullParameter(it, "it");
      return TargetFilterState.copy$default(it, (TargetFilterPreset)null, 0, $showDead, false, false, 27, (Object)null);
   }

   private static final TargetFilterState setShowStale$lambda$41(boolean $showStale, TargetFilterState it) {
      Intrinsics.checkNotNullParameter(it, "it");
      return TargetFilterState.copy$default(it, (TargetFilterPreset)null, 0, false, $showStale, false, 23, (Object)null);
   }

   private static final TargetFilterState setFocusMode$lambda$42(boolean $enabled, TargetFilterState it) {
      Intrinsics.checkNotNullParameter(it, "it");
      return TargetFilterState.copy$default(it, (TargetFilterPreset)null, 0, false, false, $enabled, 15, (Object)null);
   }

   private static final String assignTeamMemberRolesBulk$lambda$51(String it) {
      Intrinsics.checkNotNullParameter(it, "it");
      return StringsKt.trim((CharSequence)it).toString();
   }

   private static final boolean assignTeamMemberRolesBulk$lambda$52(String $actorUid, String it) {
      Intrinsics.checkNotNullParameter(it, "it");
      return !StringsKt.isBlank((CharSequence)it) && !Intrinsics.areEqual(it, $actorUid);
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
      Intrinsics.checkNotNullParameter(filterState, "filterState");
      return this$0.targetFilterCoordinator.toMemberPrefs(filterState);
   }

   private static final Unit startMemberPrefsSync$lambda$81(TeamCompassViewModel this$0, TeamMemberPrefs remotePrefs) {
      TargetFilterState next = this$0.targetFilterCoordinator.fromRemotePrefs(remotePrefs);
      MutableStateFlow $this$update$iv = this$0._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         UiState state = (UiState)prevValue$iv;
         int var7 = 0;
         nextValue$iv = Intrinsics.areEqual(state.getFilter().getTargetFilterState(), next) ? state : UiState.copy$default(state, (AuthState)null, (TrackingUiState)null, (TeamUiState)null, (MapUiState)null, FilterUiState.copy$default(state.getFilter(), next, (List)null, (List)null, 6, (Object)null), (SettingsUiState)null, (BluetoothUiState)null, (String)null, 239, (Object)null);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      refreshTargetsFromState$default(this$0, 0L, 1, (Object)null);
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
      Intrinsics.checkNotNullParameter(failure, "failure");
      if (userInitiated) {
         this$0.emitError(this$0.tr(string.vm_error_targets_filter_save_failed), failure.getCause());
      } else {
         Log.w("TeamCompassVM", "Background memberPrefs sync failed: " + failure.getError());
      }

      return Unit.INSTANCE;
   }

   private static final UiState startLocationServiceMonitor$lambda$96(TeamCompassViewModel this$0) {
      return (UiState)this$0._ui.getValue();
   }

   private static final Unit startLocationServiceMonitor$lambda$97(TeamCompassViewModel this$0, Function1 transform) {
      Intrinsics.checkNotNullParameter(transform, "transform");
      MutableStateFlow $this$update$iv = this$0._ui;
      int $i$f$update = 0;

      Object prevValue$iv;
      Object nextValue$iv;
      do {
         prevValue$iv = $this$update$iv.getValue();
         nextValue$iv = transform.invoke(prevValue$iv);
      } while(!$this$update$iv.compareAndSet(prevValue$iv, nextValue$iv));

      return Unit.INSTANCE;
   }

   private static final Unit startLocationServiceMonitor$lambda$98(TeamCompassViewModel this$0, String message) {
      Intrinsics.checkNotNullParameter(message, "message");
      emitError$default(this$0, message, (Throwable)null, 2, (Object)null);
      return Unit.INSTANCE;
   }

   // $FF: synthetic method
   public static final UserPrefs access$getPrefs$p(TeamCompassViewModel $this) {
      return $this.prefs;
   }

   // $FF: synthetic method
   public static final MutableStateFlow access$get_ui$p(TeamCompassViewModel $this) {
      return $this._ui;
   }

   // $FF: synthetic method
   public static final String access$normalizeTeamCode(TeamCompassViewModel $this, String raw) {
      return $this.normalizeTeamCode(raw);
   }

   // $FF: synthetic method
   public static final void access$startListening(TeamCompassViewModel $this, String codeRaw) {
      $this.startListening(codeRaw);
   }

   // $FF: synthetic method
   public static final void access$stopListening(TeamCompassViewModel $this) {
      $this.stopListening();
   }

   // $FF: synthetic method
   public static final AutoBrightnessBinding access$getAutoBrightnessBinding$p(TeamCompassViewModel $this) {
      return $this.autoBrightnessBinding;
   }

   // $FF: synthetic method
   public static final TrackingController access$getTrackingController$p(TeamCompassViewModel $this) {
      return $this.trackingController;
   }

   // $FF: synthetic method
   public static final MapCoordinator access$getMapCoordinator$p(TeamCompassViewModel $this) {
      return $this.mapCoordinator;
   }

   // $FF: synthetic method
   public static final Application access$getApplication$p(TeamCompassViewModel $this) {
      return $this.application;
   }

   // $FF: synthetic method
   public static final String access$tr(TeamCompassViewModel $this, int resId, Object... args) {
      return $this.tr(resId, args);
   }

   // $FF: synthetic method
   public static final TeamRepository access$getTeamRepository$p(TeamCompassViewModel $this) {
      return $this.teamRepository;
   }

   // $FF: synthetic method
   public static final void access$handleActionFailure(TeamCompassViewModel $this, String defaultMessage, TeamActionFailure failure) {
      $this.handleActionFailure(defaultMessage, failure);
   }

   // $FF: synthetic method
   public static final TeamSessionDelegate access$getTeamSessionDelegate$p(TeamCompassViewModel $this) {
      return $this.teamSessionDelegate;
   }

   // $FF: synthetic method
   public static final Object access$onTeamJoined(TeamCompassViewModel $this, String code, Continuation $completion) {
      return $this.onTeamJoined(code, $completion);
   }

   // $FF: synthetic method
   public static final void access$evaluateIdentityLinkingEligibility(TeamCompassViewModel $this, String teamCode) {
      $this.evaluateIdentityLinkingEligibility(teamCode);
   }

   // $FF: synthetic method
   public static final void access$logActionSuccess(TeamCompassViewModel $this, String action, String traceId, String teamCode, String uid) {
      $this.logActionSuccess(action, traceId, teamCode, uid);
   }

   // $FF: synthetic method
   public static final void access$logActionFailure(TeamCompassViewModel $this, String action, String traceId, Throwable throwable, String message, String teamCode, String uid) {
      $this.logActionFailure(action, traceId, throwable, message, teamCode, uid);
   }

   // $FF: synthetic method
   public static final boolean access$handleStartListeningTerminalFailure(TeamCompassViewModel $this, TeamActionFailure failure, String teamCode, String uid, String traceId) {
      return $this.handleStartListeningTerminalFailure(failure, teamCode, uid, traceId);
   }

   // $FF: synthetic method
   public static final void access$startBackendHealthMonitor(TeamCompassViewModel $this) {
      $this.startBackendHealthMonitor();
   }

   // $FF: synthetic method
   public static final void access$startBackendStaleMonitor(TeamCompassViewModel $this) {
      $this.startBackendStaleMonitor();
   }

   // $FF: synthetic method
   public static final void access$startP2PInboundObservation(TeamCompassViewModel $this, String teamCode, String localUid) {
      $this.startP2PInboundObservation(teamCode, localUid);
   }

   // $FF: synthetic method
   public static final Object access$collectTeamSnapshotsWithReconnect(TeamCompassViewModel $this, String teamCode, String uid, Continuation $completion) {
      return $this.collectTeamSnapshotsWithReconnect(teamCode, uid, $completion);
   }

   // $FF: synthetic method
   public static final void access$scheduleBackendStaleRefresh(TeamCompassViewModel $this, long nowMs) {
      $this.scheduleBackendStaleRefresh(nowMs);
   }

   // $FF: synthetic method
   public static final void access$processEnemyPingAlerts(TeamCompassViewModel $this, List enemyPings) {
      $this.processEnemyPingAlerts(enemyPings);
   }

   // $FF: synthetic method
   public static final void access$processSosAlerts(TeamCompassViewModel $this) {
      $this.processSosAlerts();
   }

   // $FF: synthetic method
   public static final boolean access$computeBackendStale(TeamCompassViewModel $this, long lastSnapshotAtMs, long nowMs) {
      return $this.computeBackendStale(lastSnapshotAtMs, nowMs);
   }

   // $FF: synthetic method
   public static final void access$handleP2PInbound(TeamCompassViewModel $this, P2PInboundMessage inbound, String localUid) {
      $this.handleP2PInbound(inbound, localUid);
   }

   // $FF: synthetic method
   public static final String access$newTraceId(TeamCompassViewModel $this, String action) {
      return $this.newTraceId(action);
   }

   // $FF: synthetic method
   public static final void access$setLastBackendHealthAvailableSample$p(TeamCompassViewModel $this, boolean var1) {
      $this.lastBackendHealthAvailableSample = var1;
   }

   // $FF: synthetic method
   public static final void access$vibrateAndBeep(TeamCompassViewModel $this, boolean strong) {
      $this.vibrateAndBeep(strong);
   }

   // $FF: synthetic method
   public static final void access$logActionStart(TeamCompassViewModel $this, String action, String traceId, String teamCode, String uid) {
      $this.logActionStart(action, traceId, teamCode, uid);
   }

   // $FF: synthetic method
   public static final Function2 access$getInitializeAutoStartOverride$p(TeamCompassViewModel $this) {
      return $this.initializeAutoStartOverride;
   }

   // $FF: synthetic method
   public static final void access$initializeAutoStart(TeamCompassViewModel $this) {
      $this.initializeAutoStart();
   }
}
