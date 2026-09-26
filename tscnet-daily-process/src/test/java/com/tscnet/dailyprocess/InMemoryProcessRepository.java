package com.tscnet.dailyprocess;

import com.tscnet.dailyprocess.model.DailyProcess;
import com.tscnet.dailyprocess.repository.DailyProcessRepository;

import java.time.LocalDate;
import java.util.*;

public class InMemoryProcessRepository implements DailyProcessRepository {
    private final Map<LocalDate, DailyProcess> data = new HashMap<>();

    @Override public Optional<DailyProcess> findByBusinessDate(LocalDate date) { return Optional.ofNullable(data.get(date)); }

    @Override public <S extends DailyProcess> S save(S entity) {
        data.put(entity.getBusinessDate(), entity);
        return entity;
    }

    @Override public List<DailyProcess> findAll() { return new ArrayList<>(data.values()); }
    @Override public <S extends DailyProcess> List<S> saveAll(Iterable<S> entities) { List<S> r = new ArrayList<>(); entities.forEach(e -> r.add(save(e))); return r; }
    @Override public Optional<DailyProcess> findById(Long id) { return data.values().stream().filter(x -> Objects.equals(x.getId(), id)).findFirst(); }
    @Override public boolean existsById(Long id) { return findById(id).isPresent(); }
    @Override public long count() { return data.size(); }
    @Override public void deleteById(Long id) { data.values().removeIf(x -> Objects.equals(x.getId(), id)); }
    @Override public void delete(DailyProcess entity) { data.remove(entity.getBusinessDate()); }
    @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(this::deleteById); }
    @Override public void deleteAll(Iterable<? extends DailyProcess> entities) { entities.forEach(this::delete); }
    @Override public void deleteAll() { data.clear(); }
    @Override public List<DailyProcess> findAllById(Iterable<Long> ids) { throw new UnsupportedOperationException(); }
    @Override public List<DailyProcess> findAll(org.springframework.data.domain.Sort sort) { throw new UnsupportedOperationException(); }
    @Override public org.springframework.data.domain.Page<DailyProcess> findAll(org.springframework.data.domain.Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public <S extends DailyProcess> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends DailyProcess> List<S> findAll(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends DailyProcess> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { throw new UnsupportedOperationException(); }
    @Override public <S extends DailyProcess> long count(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends DailyProcess> boolean exists(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
}
