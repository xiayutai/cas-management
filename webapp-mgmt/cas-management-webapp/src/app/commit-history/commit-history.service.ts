/**
 * Created by tschmidt on 2/13/17.
 */
import {Injectable} from '@angular/core';
import {History} from '../../domain/history';
import {Service} from '../service';
import {Http} from '@angular/http';
import {HttpClient} from '@angular/common/http';
import {DiffEntry} from '../../domain/diff-entry';

@Injectable()
export class CommitHistoryService extends Service {

  constructor(protected http: HttpClient) {
    super(http);
  }

  history(id: string): Promise<DiffEntry[]> {
    return this.get<DiffEntry[]>('commitHistoryList?id=' + id);
  }

  checkout(id: string, path: String): Promise<String> {
    return this.getText('checkout?id=' + id + '&path=' + path);
  }

  revertRepo(id: string): Promise<String> {
    return this.getText('revertRepo?id=' + id);
  }
}
