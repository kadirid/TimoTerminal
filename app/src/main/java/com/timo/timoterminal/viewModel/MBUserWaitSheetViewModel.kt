package com.timo.timoterminal.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.modalBottomSheets.MBUserWaitSheet
import com.timo.timoterminal.service.UserService

class MBUserWaitSheetViewModel(
    private val userService: UserService
):ViewModel() {

    fun updateUser(paramMap: HashMap<String, String>, sheet: MBUserWaitSheet) {
        userService.sendUpdateRequestSheet(paramMap, sheet, viewModelScope)
    }

}