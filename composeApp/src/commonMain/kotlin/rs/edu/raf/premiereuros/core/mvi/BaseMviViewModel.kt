package rs.edu.raf.premiereuros.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch

abstract class BaseMviViewModel<State : MviState, Event : MviEvent, Effect : MviEffect>(
    initialState: State
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 32)
    val eventFlow = _events.asSharedFlow()

    private val _effects = MutableSharedFlow<Effect>()
    val effects = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            eventFlow.collect(::onEvent)
        }
    }

    fun setEvent(event: Event) {
        if (!_events.tryEmit(event)) {
            viewModelScope.launch { _events.emit(event) }
        }
    }

    protected abstract suspend fun onEvent(event: Event)

    protected fun setState(reducer: State.() -> State) {
        _state.getAndUpdate(reducer)
    }

    protected suspend fun emitEffect(effect: Effect) {
        _effects.emit(effect)
    }

    protected val currentState: State
        get() = _state.value

    protected fun replaceState(newState: State) {
        _state.value = newState
    }
}
