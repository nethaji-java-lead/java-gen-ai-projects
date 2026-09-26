package com.tscnet.dailyprocess;

import com.tscnet.dailyprocess.model.ProcessExecution;
import com.tscnet.dailyprocess.model.ExecutionStatus;
import com.tscnet.dailyprocess.repository.ProcessExecutionRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryExecutionRepository implements ProcessExecutionRepository {
    private final Map<Long, ProcessExecution> data = new LinkedHashMap<>();
    private final AtomicLong ids = new AtomicLong(1);

    @Override public <S extends ProcessExecution> S save(S entity) {
        try {
            var field = ProcessExecution.class.getDeclaredField("id");
            field.setAccessible(true);
            if (field.get(entity) == null) field.set(entity, ids.getAndIncrement());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        data.put(entity.getId(), entity);
        return entity;
    }

    @Override public List<ProcessExecution> findAllByOrderByInitiatedAtDesc() {
        return data.values().stream().sorted(Comparator.comparing(ProcessExecution::getInitiatedAt).reversed()).toList();
    }

    @Override public List<ProcessExecution> findByBusinessDateOrderByInitiatedAtDesc(LocalDate date) {
        return data.values().stream().filter(x -> date.equals(x.getBusinessDate()))
                .sorted(Comparator.comparing(ProcessExecution::getInitiatedAt).reversed()).toList();
    }

    @Override public List<ProcessExecution> findByBusinessDateAndStatusOrderByInitiatedAtDesc(LocalDate date, ExecutionStatus status) {
        return data.values().stream().filter(x -> date.equals(x.getBusinessDate()) && status == x.getStatus())
                .sorted(Comparator.comparing(ProcessExecution::getInitiatedAt).reversed()).toList();
    }

    @Override public Optional<ProcessExecution> findById(Long id) { return Optional.ofNullable(data.get(id)); }
    @Override public List<ProcessExecution> findAll() { return new ArrayList<>(data.values()); }
    @Override public List<ProcessExecution> findAllById(Iterable<Long> ids) { throw new UnsupportedOperationException(); }
    @Override public long count() { return data.size(); }
    @Override public void deleteById(Long id) { data.remove(id); }
    @Override public void delete(ProcessExecution entity) { data.remove(entity.getId()); }
    @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(data::remove); }
    @Override public void deleteAll(Iterable<? extends ProcessExecution> entities) { entities.forEach(e -> data.remove(e.getId())); }
    @Override public void deleteAll() { data.clear(); }
    @Override public boolean existsById(Long id) { return data.containsKey(id); }
    @Override public <S extends ProcessExecution> List<S> saveAll(Iterable<S> entities) { List<S> r = new ArrayList<>(); entities.forEach(e -> r.add(save(e))); return r; }
    @Override public <S extends ProcessExecution> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends ProcessExecution> List<S> findAll(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends ProcessExecution> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { throw new UnsupportedOperationException(); }
    @Override public <S extends ProcessExecution> long count(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends ProcessExecution> boolean exists(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public Optional<ProcessExecution> findOne(org.springframework.data.domain.Example<ProcessExecution> example) { throw new UnsupportedOperationException(); }
    @Override public List<ProcessExecution> findAll(org.springframework.data.domain.Sort sort) { throw new UnsupportedOperationException(); }
    @Override public List<ProcessExecution> findAll(org.springframework.data.domain.PageRequest page) { throw new UnsupportedOperationException(); }
    @Override public org.springframework.data.domain.Page<ProcessExecution> findAll(org.springframework.data.domain.Pageable pageable) { throw new UnsupportedOperationException(); }
}
