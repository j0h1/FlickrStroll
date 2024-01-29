package com.imjori.flickrstroll.presentation

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.imjori.flickrstroll.R
import com.imjori.flickrstroll.data.location.NotificationService
import com.imjori.flickrstroll.presentation.FlickrStrollViewModel.Event.RestartLocationPollingService
import com.imjori.flickrstroll.presentation.FlickrStrollViewModel.Event.WalkStarted
import com.imjori.flickrstroll.presentation.FlickrStrollViewModel.Event.WalkStopped
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FlickrScrollScreen(
    modifier: Modifier = Modifier,
    viewModel: FlickrStrollViewModel = hiltViewModel()
) {
    val state = viewModel.viewState.value
    val scaffoldState = rememberScaffoldState()

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    ) { permissionsResult ->
        val locationGranted = permissionsResult[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
                && permissionsResult[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        when {
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && permissionsResult.values.all { it })
                    || (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && locationGranted) -> {
                viewModel.toggleWalk()
            }
        }
    }


    val context = LocalContext.current as MainActivity

    LaunchedEffect(true) {
        viewModel.event.collectLatest { event ->
            when (event) {
                WalkStarted, RestartLocationPollingService -> {
                    context.startForegroundService(NotificationService.getIntent(context))
                }

                WalkStopped -> {
                    context.stopService(NotificationService.getIntent(context))
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                modifier = modifier,
                title = {},
                backgroundColor = Color.White,
                actions = {
                    Button(
                        colors = ButtonDefaults.outlinedButtonColors(),
                        onClick = {
                            val locationPermissionsGranted = permissionState.permissions.filter {
                                it.permission in listOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            }.all { it.status.isGranted }
                            when {
                                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionState.allPermissionsGranted)
                                        || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && !locationPermissionsGranted -> {
                                    permissionState.launchMultiplePermissionRequest()
                                }

                                else -> viewModel.toggleWalk()
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(
                                id = if (state.isWalkStarted) R.string.stop else R.string.start
                            ).uppercase(),
                        )
                    }
                }
            )
        },
        scaffoldState = scaffoldState
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(state.photos) { photo ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photo.url)
                        .crossfade(500)
                        .build(),
                    contentDescription = photo.contentDescription,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(all = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
