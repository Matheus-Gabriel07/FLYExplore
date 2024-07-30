package com.example.flightsearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.data.FlightSearchRepository
import com.example.flightsearch.data.IataAndName
import com.example.flightsearch.data.UserPreferencesRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FlightSearchUIState(
    val userInput: String = "",
    val selectedAirport: IataAndName = IataAndName(iataCode = "", name =  ""),
    val isAirportSelected: Boolean = false,
    val flightSavedStates: MutableMap<Favorite, Boolean> = mutableMapOf(),
    val isDeleteDialogVisible: Boolean = false
)

@OptIn(FlowPreview::class)
class FlightSearchViewModel(
    private val flightSearchRepository: FlightSearchRepository,
    private val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(
        FlightSearchUIState()
    )

    val uiState: StateFlow<FlightSearchUIState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    userInput = userPreferencesRepository.userInput.first()
                )
            }
        }
    }

    // Atualiza o que o usuário digitou e a salva no repositório de preferências do usuário
    fun updateUserInput(input: String) {
        _uiState.update {
            it.copy(
                userInput = input,
                isAirportSelected = false
            )
        }
        viewModelScope.launch {
            userPreferencesRepository.saveUserInput(input)
        }
    }

    // Atualiza o aeroporto selecionado para que a função possa retornar uma lista de voos possíveis com base nele
    fun updateSelectedAirport(updatedSelectedAirport: IataAndName) {
        _uiState.update {
            it.copy(
                selectedAirport = updatedSelectedAirport,
                isAirportSelected = true
            )
        }
    }

    // Recupera sugestões de preenchimento automático conforme o usuário preenche a barra de pesquisa
    fun retrieveAutocompleteSuggestions(): Flow<List<IataAndName>> {
        return if (_uiState.value.userInput.isNotBlank())
            flightSearchRepository.getAutocompleteSuggestions(_uiState.value.userInput.trim()).debounce(500L)
        else
            emptyFlow()
    }

    // Recupera a lista de possíveis voos após o aeroporto ser selecionado
    fun retrievePossibleFlights(selectedAirport: IataAndName): Flow<List<IataAndName>> =
        flightSearchRepository.getPossibleFlights(selectedAirport.iataCode, selectedAirport.name)

    // Marca o voo como salvo ou excluído alterando o valor booleano para verdadeiro ou falso dependendo do valor booleano
    private fun updateFlightSavedState(favorite: Favorite, newState: Boolean) {
        _uiState.update {
            it.copy(
                flightSavedStates = _uiState.value.flightSavedStates.toMutableMap().apply {
                    this[favorite] = newState
                }
            )
        }
    }

    // Salva o item no banco de dados local e marca-o como favorito
    fun insertItem(favorite: Favorite) {
        updateFlightSavedState(favorite, true)

        viewModelScope.launch {
            flightSearchRepository.insertFavoriteItem(favorite)
        }
    }

    // Exclui item do banco de dados local e marca-o como excluído
    fun deleteItem(favorite: Favorite) {
        if (_uiState.value.flightSavedStates[favorite] == true)
            updateFlightSavedState(favorite, false)

        viewModelScope.launch {
            flightSearchRepository.deleteFavorite(favorite.departureCode, favorite.destinationCode)
        }
    }

    // Verifica se o voo está salvo ou não
    fun isFlightSaved(favorite: Favorite): Boolean {
        return _uiState.value.flightSavedStates[favorite] == true
    }

    // Retorna uma lista de itens favoritos (salvos) do banco de dados
    fun getAllFavorites(): Flow<List<Favorite>> =
        flightSearchRepository.getAllFavorites()

    // Deleta todos os voos favoritados
    suspend fun deleteAllFavorites() {
        _uiState.value.flightSavedStates.forEach { (favorite) ->
            _uiState.value.flightSavedStates[favorite] = false
        }
        flightSearchRepository.deleteAllFavorites()
    }

    // Torna a caixa de diálogo de exclusão visível ou invisível
    fun toggleDeleteDialogVisibility() {
        _uiState.update {
            it.copy(
                isDeleteDialogVisible = !it.isDeleteDialogVisible
            )
        }
    }

    // Limpa a entrada do usuário da barra de pesquisa e a salva no repositório de preferências
    fun onClearClick() {
        _uiState.update {
            it.copy(
                userInput = ""
            )
        }

        viewModelScope.launch {
            userPreferencesRepository.saveUserInput(_uiState.value.userInput)
        }
    }

    // Verifica os itens salvos e se o mesmo item existe na lista de possíveis voos, marca-o como salvo
    fun syncFavoritesWithFlights(favorites: List<Favorite>, selectedAirport: IataAndName, destinationAirports: List<IataAndName>) {
        for (favorite in favorites)
            for (destinationAirport in destinationAirports) {
                if (favorite.departureCode == selectedAirport.iataCode && favorite.destinationCode == destinationAirport.iataCode)
                    updateFlightSavedState(Favorite(departureCode = selectedAirport.iataCode, destinationCode = destinationAirport.iataCode), true)
            }
    }
}