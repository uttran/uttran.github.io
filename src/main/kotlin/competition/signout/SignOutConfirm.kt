/*
 * Copyright 2020 Uttran Ishtalingam
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package competition.signout

import kotlinx.css.fontSize
import kotlinx.css.pct
import kotlinx.css.rem
import kotlinx.css.width
import kotlinx.html.js.onClickFunction
import react.*
import styled.css
import styled.styledButton
import styled.styledDiv
import styled.styledP

external interface SignOutConfirmProps: RProps {
    var onNoClickHandler: () -> Unit
    var onYesClickHandler: () -> Unit
}

class SignOutConfirm : RComponent<SignOutConfirmProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                classes = mutableListOf("container-md")
            }
            styledDiv {
                css {
                    classes = mutableListOf("row")
                }
                styledDiv {
                    css {
                        classes = mutableListOf("d-flex justify-content-center align-items-center pb-5")
                        width = 100.pct
                    }
                    styledDiv {
                        css {
                            classes = mutableListOf("card bg-light m-2")
                        }
                        styledDiv {
                            css {
                                classes = mutableListOf("card-header")
                                fontSize = 1.5.rem
                            }
                            +"Going back to Home"
                        }
                        styledDiv {
                            css {
                                classes = mutableListOf("card-body")
                            }
                            styledP {
                                css {
                                    classes = mutableListOf("card-text")
                                }
                                +"Once you click yes, you can't see the scores of this student"
                            }
                            styledP {
                                css {
                                    classes = mutableListOf("card-text")
                                }
                                +"Do you really want to proceed?"
                            }
                        }
                        styledDiv {
                            css {
                                classes = mutableListOf("card-footer")
                            }
                            styledDiv {
                                css {
                                    classes = mutableListOf("d-flex flex-row-reverse bd-highlight")
                                }
                                styledButton {
                                    css {
                                        classes = mutableListOf("btn btn-primary")
                                    }
                                    +"No"
                                    attrs {
                                        onClickFunction = { props.onNoClickHandler() }
                                    }
                                }
                                styledButton {
                                    css {
                                        classes = mutableListOf("btn btn-secondary mr-2")
                                    }
                                    +"Yes"
                                    attrs {
                                        onClickFunction = { props.onYesClickHandler() }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun RBuilder.signOutConfirm(handler: SignOutConfirmProps.() -> Unit): ReactElement {
    return child(SignOutConfirm::class) {
        this.attrs(handler)
    }
}
