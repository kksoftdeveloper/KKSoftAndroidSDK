package com.appmb.sdk.mbauth.ui.server

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.appmb.sdk.mbcore.data.dto.response.MbServer
import com.appmb.sdk.mbauth.ui.frame.MbListServersFrameContainer
import com.appmb.sdk.mbauth.ui.login.GetGameUuidState
import com.appmb.sdk.mbauth.ui.server.components.MaintenancePopupView
import com.appmb.sdk.mbauth.ui.server.components.ServerItemView
import com.appmb.sdk.mbcore.model.MbAuthData
import com.appmb.sdk.mbcoreui.ToastHostState
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
  isEnableClose: Boolean,
  onClose: () -> Unit,
  onComplete: (MbAuthData) -> Unit,
) {
  var selected by remember { mutableStateOf<MbServer?>(null) }

  val viewModel: ServerViewModel = koinViewModel()
  val serverState = viewModel.serverState.collectAsState()
  val getGameUuidState = viewModel.getGameUuidState.collectAsState()
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  val toastHostState = remember {
    ToastHostState()
  }
//  LaunchedEffect(Unit) {
//    viewModel.onAction(ServerListAction.GetListServerIds)
//  }
  MbListServersFrameContainer(
    isEnableClose = isEnableClose,
    onClose = {
      onClose()
    },
    buttonEnabledState = selected != null
        && selected?.status.orEmpty() != MbServer.SERVER_MAINTENANCE
        && selected?.status.orEmpty() != MbServer.SERVER_OFFLINE,
    onButtonClick = {
      selected?.let {
        viewModel.onAction(ServerListAction.GetGameUuid(it.serverId.toString()))
      }
    },
    isLoading = uiState.isLoading,
    toastHostState = toastHostState,
  ) {
    when (val state = getGameUuidState.value) {
      is GetGameUuidState.Error -> {
        toastHostState.showToast(message = state.message)
      }

      is GetGameUuidState.Success -> {
        onComplete(state.mbAuthData)
      }

      else -> Unit
    }

    when (val state = serverState.value) {
      is ServerOperationState.Error -> {
        toastHostState.showToast(
          message = state.message,
        )
      }

      is ServerOperationState.GetServerIdsSuccess -> {
        LazyColumn(
          modifier = Modifier
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          items(items = state.servers, key = { it.serverId ?: 0 }) { server ->
            Box(
              modifier = Modifier
                .padding(vertical = 2.dp),
            ) {
              ServerItemView(
                server = server, isSelected = server == selected, onClick = { selected = server })
            }
          }
        }
      }

      ServerOperationState.Maintenance -> {
        MaintenancePopupView(
          onClose = {
            onClose()
          }
        )
      }

      ServerOperationState.Idle -> Unit
      is ServerOperationState.UpdateServerClientIdSuccess -> {
        Log.d("ServerListScreen", "UpdateServerClientIdSuccess: ${state.authData}, charId: ${state.characterId}" )
      }
      is ServerOperationState.UpdateServerClientIdUnauthenticated -> {
        Log.d("ServerListScreen", "UpdateServerClientIdUnauthenticated: ${state.message}")
      }
    }
  }
}
