/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.bpm.mock;

import org.kie.api.KieBase;
import org.kie.api.command.Command;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.Calendars;
import org.kie.api.runtime.Channel;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.Globals;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.rule.Agenda;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.LiveQuery;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.ViewChangedEventListener;
import org.kie.api.time.SessionClock;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/26/16 Time: 1:25 PM
 */
@SuppressWarnings("WeakerAccess")
public class MockKieSession implements KieSession {

    private BiFunction<String, Map, ProcessInstance> onStartProcess;

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public long getIdentifier() {
        return 0;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public <T extends SessionClock> T getSessionClock() {
        return null;
    }

    @Override
    public void setGlobal(String s, Object o) {

    }

    @Override
    public Object getGlobal(String s) {
        return null;
    }

    @Override
    public Globals getGlobals() {
        return null;
    }

    @Override
    public Calendars getCalendars() {
        return null;
    }

    @Override
    public Environment getEnvironment() {
        return null;
    }

    @Override
    public KieBase getKieBase() {
        return null;
    }

    @Override
    public void registerChannel(String s, Channel channel) {

    }

    @Override
    public void unregisterChannel(String s) {

    }

    @Override
    public Map<String, Channel> getChannels() {
        return null;
    }

    @Override
    public KieSessionConfiguration getSessionConfiguration() {
        return null;
    }

    @Override
    public KieRuntimeLogger getLogger() {
        return null;
    }

    @Override
    public void addEventListener(ProcessEventListener processEventListener) {

    }

    @Override
    public void removeEventListener(ProcessEventListener processEventListener) {

    }

    @Override
    public Collection<ProcessEventListener> getProcessEventListeners() {
        return null;
    }

    @Override
    public void addEventListener(RuleRuntimeEventListener ruleRuntimeEventListener) {

    }

    @Override
    public void removeEventListener(RuleRuntimeEventListener ruleRuntimeEventListener) {

    }

    @Override
    public Collection<RuleRuntimeEventListener> getRuleRuntimeEventListeners() {
        return null;
    }

    @Override
    public void addEventListener(AgendaEventListener agendaEventListener) {

    }

    @Override
    public void removeEventListener(AgendaEventListener agendaEventListener) {

    }

    @Override
    public Collection<AgendaEventListener> getAgendaEventListeners() {
        return null;
    }

    @Override
    public <T> T execute(Command<T> command) {
        return null;
    }

    @Override
    public ProcessInstance startProcess(String s) {
        return null;
    }

    @Override
    public ProcessInstance startProcess(String s, Map<String, Object> map) {
        return onStartProcess.apply(s, map);
    }

    @Override
    public ProcessInstance createProcessInstance(String s, Map<String, Object> map) {
        return null;
    }

    @Override
    public ProcessInstance startProcessInstance(long l) {
        return null;
    }

    @Override
    public void signalEvent(String s, Object o) {

    }

    @Override
    public void signalEvent(String s, Object o, long l) {

    }

    @Override
    public Collection<ProcessInstance> getProcessInstances() {
        return null;
    }

    @Override
    public ProcessInstance getProcessInstance(long l) {
        return null;
    }

    @Override
    public ProcessInstance getProcessInstance(long l, boolean b) {
        return null;
    }

    @Override
    public void abortProcessInstance(long l) {

    }

    @Override
    public WorkItemManager getWorkItemManager() {
        return null;
    }

    @Override
    public void halt() {

    }

    @Override
    public Agenda getAgenda() {
        return null;
    }

    @Override
    public EntryPoint getEntryPoint(String s) {
        return null;
    }

    @Override
    public Collection<? extends EntryPoint> getEntryPoints() {
        return null;
    }

    @Override
    public QueryResults getQueryResults(String s, Object... objects) {
        return null;
    }

    @Override
    public LiveQuery openLiveQuery(String s, Object[] objects, ViewChangedEventListener viewChangedEventListener) {
        return null;
    }

    @Override
    public String getEntryPointId() {
        return null;
    }

    @Override
    public FactHandle insert(Object o) {
        return null;
    }

    @Override
    public void retract(FactHandle factHandle) {

    }

    @Override
    public void delete(FactHandle factHandle) {

    }

    @Override
    public void update(FactHandle factHandle, Object o) {

    }

    @Override
    public FactHandle getFactHandle(Object o) {
        return null;
    }

    @Override
    public Object getObject(FactHandle factHandle) {
        return null;
    }

    @Override
    public Collection<? extends Object> getObjects() {
        return null;
    }

    @Override
    public Collection<? extends Object> getObjects(ObjectFilter objectFilter) {
        return null;
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles() {
        return null;
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles(ObjectFilter objectFilter) {
        return null;
    }

    @Override
    public long getFactCount() {
        return 0;
    }

    @Override
    public int fireAllRules() {
        return 0;
    }

    @Override
    public int fireAllRules(int i) {
        return 0;
    }

    @Override
    public int fireAllRules(AgendaFilter agendaFilter) {
        return 0;
    }

    @Override
    public int fireAllRules(AgendaFilter agendaFilter, int i) {
        return 0;
    }

    @Override
    public void fireUntilHalt() {

    }

    @Override
    public void fireUntilHalt(AgendaFilter agendaFilter) {

    }

    public void onStartProcess(BiFunction<String, Map, ProcessInstance> onStartProcess) {
        this.onStartProcess = onStartProcess;
    }

    @Override
    public void submit(AtomicAction action) {

    }

    @Override
    public void delete(FactHandle fh, FactHandle.State state) {

    }

    @Override
    public void update(FactHandle fh, Object o, String... strings) {

    }
}
