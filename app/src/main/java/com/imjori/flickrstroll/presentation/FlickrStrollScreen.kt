package com.imjori.flickrstroll.presentation

import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
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
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.imjori.flickrstroll.R
import com.imjori.flickrstroll.data.location.LocationPollingService
import com.imjori.flickrstroll.presentation.util.Event
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FlickrScrollScreen(
    modifier: Modifier = Modifier,
    viewModel: FlickrStrollViewModel = hiltViewModel()
) {
    val state = viewModel.viewState.value
    val scaffoldState = rememberScaffoldState()

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    ) { permissionStateMap ->
        val allPermissionsGranted = !permissionStateMap.containsValue(false)
        if (allPermissionsGranted) {
            viewModel.toggleWalk()
        }
    }

    val context = LocalContext.current as MainActivity

    LaunchedEffect(true) {
        viewModel.event.collectLatest { event ->
            when (event) {
                Event.WalkStarted, Event.RestartLocationPollingService -> {
                    context.startForegroundService(LocationPollingService.getIntent(context))
                }
                Event.WalkStopped -> {
                    context.stopService(LocationPollingService.getIntent(context))
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
                            if (!locationPermissionState.allPermissionsGranted) {
                                locationPermissionState.launchMultiplePermissionRequest()
                            } else {
                                viewModel.toggleWalk()
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
