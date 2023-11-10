import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.css.Position
import kotlinx.css.position
import kotlinx.css.px
import kotlinx.css.right
import kotlinx.css.top
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.setState
import styled.css
import styled.styledA
import styled.styledDiv

private const val sourceRepo = "https://raw.githubusercontent.com/dreamuth/dreamuth.github.io"

suspend fun fetchSource(): List<Thirukkural> {
    val sourceUrl = "$sourceRepo/master/resources/thirukkural.json"
    val sourceData = window.fetch(sourceUrl).await().text().await()
    val groupsUrl = "$sourceRepo/master/kids/resources/kids-group-2022-23.json"
    val groupsData = window.fetch(groupsUrl).await().text().await()

    val thirukkurals = parseSource(sourceData, groupsData)
    println("version: 2023-11-09.2")
    println("Source: $sourceUrl loaded")
    return thirukkurals
}

external interface AppState: RState {
    var loaded: Boolean
    var thirukkurals: MutableMap<Group, List<Thirukkural>>
    var selectedKuralMeaning: MutableSet<KuralMeaning>
    var questionState: QuestionState
}

private const val baseUrl = "tamilschool.github.io"

class App : RComponent<RProps, AppState>() {
    override fun AppState.init() {
        val mainScope = MainScope()
        mainScope.launch {
            val source = fetchSource()
            val group2Kurals = source.filter { it.group.contains(Group.II) }
            val group3Kurals = source.filter { it.group.contains(Group.III) }
            setState {
                thirukkurals = mutableMapOf()
                thirukkurals[Group.II] = group2Kurals
                thirukkurals[Group.III] = group3Kurals
                selectedKuralMeaning = mutableSetOf(KuralMeaning.SalamanPapa)
                questionState = createQuestionState(Group.II, group2Kurals)
                loaded = true
                println("No of athikarams: ${questionState.athikaramState.athikarams.size}")
                println("No of kurals: ${questionState.thirukkurals.size}")
                window.setInterval(timerHandler(), 1000)
            }
        }
    }

    private fun createQuestionState(group: Group, thirukkurals: List<Thirukkural>) = QuestionState(
        selectedGroup = group,
        selectedTopic = Topic.Athikaram,
        thirukkurals = thirukkurals,
        athikaramState = AthikaramState(thirukkurals),
        thirukkuralState = ThirukkuralState(thirukkurals),
        firstWordState = FirstWordState(thirukkurals),
        lastWordState = LastWordState(thirukkurals),
        timerState = TimerState(),
        showAnswer = false
    )

    private fun timerHandler(): () -> Unit = {
        if (state.questionState.timerState.isLive
            && !state.questionState.timerState.isPaused
            && state.questionState.timerState.time > 0) {
            setState {
                questionState.timerState.time--
            }
        }
    }

    override fun RBuilder.render() {
        hotKeys {
            attrs.keyMap = MyKey(arrayOf("s", "n", "p"))
            attrs.handlers = MyHandler {
                setState {
                    when (it.asDynamic()["key"]) {
                        "s" -> onShowAnswerHandler(questionState, true)
                        "n" -> onNextClickHandler(questionState)
                        "p" -> onPreviousClickHandler(questionState)
                    }
                }
            }
            styledDiv {
                css {
                    classes = mutableListOf("alert alert-primary text-center mb-0")
                }
                +"திருக்குறள் பயிற்சி"
            }
            styledDiv {
                css {
                    classes = mutableListOf("container-lg pl-0 pr-0")
                }
                if (state.loaded) {
                    styledA {
                        attrs {
                            href = "https://$baseUrl/competition"
                        }
                        css {
                            classes = mutableListOf("badge badge-primary d-none d-sm-block")
                            position = Position.fixed
                            top = 16.px
                            right = 16.px
                        }
                        +"திருக்குறள் போட்டி"
                    }
                    person {
                        questionState = state.questionState
                        selectedKuralMeaning = state.selectedKuralMeaning
                        onGroupClick = { group ->
                            setState {
                                if (questionState.selectedGroup != group) {
                                    questionState = createQuestionState(group, thirukkurals[group].orEmpty())
                                }
                            }
                        }
                        onTopicClick = { topic ->
                            setState {
                                if (questionState.selectedTopic != topic) {
                                    questionState.showAnswer = false
                                    questionState.selectedTopic = topic
                                    resetTimer(questionState, false)
                                }
                            }
                        }
                        onShowAnswerClick = {
                            setState {
                                onShowAnswerHandler(questionState, !questionState.showAnswer)
                            }
                        }
                        onTimerClick = {
                            setState {
                                val timerState = questionState.timerState
                                when {
                                    timerState.isLive && timerState.time <= 0 -> resetTimer(questionState, true)
                                    timerState.isLive && timerState.isPaused -> timerState.isPaused = false
                                    timerState.isLive && !timerState.isPaused -> timerState.isPaused = true
                                    else -> timerState.isLive = true
                                }
                            }
                        }
                        onPreviousClick = {
                            setState {
                                onPreviousClickHandler(questionState)
                            }
                        }
                        onNextClick = {
                            setState {
                                onNextClickHandler(questionState)
                            }
                        }
                        onMuVaradhaClick = {
                            setState {
                                selectedKuralMeaning = if (selectedKuralMeaning.contains(KuralMeaning.MuVaradha)) {
                                    val tempList = selectedKuralMeaning.toMutableSet()
                                    tempList.remove(KuralMeaning.MuVaradha)
                                    tempList
                                } else {
                                    val tempList = selectedKuralMeaning.toMutableSet()
                                    tempList.add(KuralMeaning.MuVaradha)
                                    tempList
                                }
                            }
                        }
                        onSalamanPapaClick = {
                            setState {
                                selectedKuralMeaning = if (selectedKuralMeaning.contains(KuralMeaning.SalamanPapa)) {
                                    val tempList = selectedKuralMeaning.toMutableSet()
                                    tempList.remove(KuralMeaning.SalamanPapa)
                                    tempList
                                } else {
                                    val tempList = selectedKuralMeaning.toMutableSet()
                                    tempList.add(KuralMeaning.SalamanPapa)
                                    tempList
                                }
                            }
                        }
                        onMuKarunanidhiClick = {
                            setState {
                                selectedKuralMeaning = if (selectedKuralMeaning.contains(KuralMeaning.MuKarunanidhi)) {
                                    val tempList = selectedKuralMeaning.toMutableSet()
                                    tempList.remove(KuralMeaning.MuKarunanidhi)
                                    tempList
                                } else {
                                    val tempList = selectedKuralMeaning.toMutableSet()
                                    tempList.add(KuralMeaning.MuKarunanidhi)
                                    tempList
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun resetTimer(questionState: QuestionState, isLive: Boolean) {
        if (isLive) {
            onNextClickHandler(questionState)
        }
        questionState.timerState = TimerState(isLive = isLive)
        questionState.showAnswer = false
        questionState.athikaramState.clearAnswers()
        questionState.thirukkuralState.clearAnswers()
        questionState.firstWordState.clearAnswers()
        questionState.lastWordState.clearAnswers()
    }

    private fun onShowAnswerHandler(questionState: QuestionState, show: Boolean) {
        questionState.showAnswer = show
    }

    private fun onNextClickHandler(questionState: QuestionState) {
        onShowAnswerHandler(questionState, false)
        when (questionState.selectedTopic) {
            Topic.Athikaram -> questionState.timerState.count = questionState.athikaramState.goNext()
            Topic.Porul, Topic.Kural -> questionState.timerState.count = questionState.thirukkuralState.goNext()
            Topic.FirstWord -> questionState.timerState.count = questionState.firstWordState.goNext()
            Topic.LastWord -> questionState.timerState.count = questionState.lastWordState.goNext()
            Topic.AllKurals -> { /* Next button not available*/ }
        }
    }

    private fun onPreviousClickHandler(questionState: QuestionState) {
        onShowAnswerHandler(questionState, false)
        when (questionState.selectedTopic) {
            Topic.Athikaram -> questionState.athikaramState.goPrevious()
            Topic.Porul, Topic.Kural -> questionState.thirukkuralState.goPrevious()
            Topic.FirstWord -> questionState.firstWordState.goPrevious()
            Topic.LastWord -> questionState.lastWordState.goPrevious()
            Topic.AllKurals -> { /* Previous button not available*/ }
        }
    }
}
