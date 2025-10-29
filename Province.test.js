import {describe, it, beforeEach} from 'node:test';
import assert from 'node:assert/strict';
import {Province} from "./Province.js";
import {sampleProvinceData} from "./Main.js";

describe('province', () => {
  let asia;
  beforeEach(function () {
    asia = new Province(sampleProvinceData());
  })
  it('shortfall', () => {
    assert.equal(asia.shortfall, 5);
  });
  it('profit', function () {
    assert.equal(asia.profit, 230);
  })
});
